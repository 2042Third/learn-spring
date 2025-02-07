# MobileDataCollection

## 收集 trace 的副作用(side effect)问题

在 benchmark 被使用时，testcase 需要考虑是否能够连续被执行并成功地被 evaluator 识别。

我目前构想了三类 testcase：

1. testcase 不具有副作用。只需要 restart app 就能够复现的 testcase（例如导航到某个页面，例如使用计算器计算某个值）
2. testcase 的副作用在本地存储，需要 reinstall app 才能够复现的 testcase（例如为当前 app 开启夜间模式）
3. app 需要登录且 testcase 的副作用在账号中存储，必须依赖手动清理才能够复现的 testcase。

benchmark 的实现会是在每次 agent 尝试运行某个 testcase 时 reinstall app。因此：

1. 对于第一类 testcase，可以直接收集。
2. 对于第二类 testcase，收集者应当在每次收集时重装 app（或者手动清理本地的缓存）以确保没有副作用导致 trace 和 evaluator 的收集出现问题。
3. 对于第三类 testcase，如果 testcase 被执行的副作用导致 trace 收集或者 evaluator 的判断出现问题，那么应抛弃这样的 testcase。

另外因为我们之后需要为存在需要登录 testcase 的 app 收集登录脚本，在收集 testcase 时应尽量避免该 testcase 需要对应的 app 登录且登录过程过于复杂（例如需要 captcha）。
如果登录的过程仅依赖于 username 和 password，可以在每次收集 trace 之前手动登录账号（这一部分不应在 trace 中）。
另外，如果一个 testcase 需要登录（弹窗处理不算做 login，仅把输入 username 和 password 的过程视作 login），应当在腾讯文档的表格中记录下来。

## 可能遇到的问题

我们目前支持了 uiautomator2 3.x，如果你使用 uiautomator2 2.x 并在截图时遇到了问题，请重新使用 `pip install -r requirements.txt` 安装 uiautomator2 3.0.18（如果需要的话，一并重新安装 uiauto）。

## 环境相关

需要自行准备 adb 工具和 Android emulator。

python 版本为 3.11.8，在创建 conda 环境后使用 `pip install -r requirements.txt` 安装即可。

Android 版本为 Android 12.0 google APIs, x86_64, API 31

机型为 Nexus 4

## 如何使用脚本

1. 在 apk-info.csv 中存储 app name（可自行指定），app package信息。因为我们目前还没有做自动登录脚本相关，username 和 password 两列留空即可。
2. 在 task_info.json 中填写 task 相关信息：首先为 task 指定一个 id，这个 id 必须为一个数字。接着按照腾讯文档中要求为 task 创建一个 task instruction。最后指定这个 task 需要在哪些 app 中进行实现（填写 app name）。
3. 创建 apks 文件夹，将安装包改名为 {app name}.apk（这里的 app name 即在 apk-info.csv中填写的）放入 apks 文件夹。
4. 在终端下输入 `python tool.py`，会要求首先输入前面指定的 task id 和 apk name，即这次收集的 trace。之后 tool 会自动启动 app（详见脚本的主函数）。
5. 第一步是收集 trace（详见脚本的 CollectTrace 函数）：
   - 步骤：
     1. 在处理好登录、弹窗等后，输入回车，由脚本记录 UI Hierarchy 和 Screenshot。
     2. 输入 action type。如果当前状态已经完成所有任务，应当输入 stop 结束 trace 收集。
     3. 对于需要与控件交互的动作输入控件的 bounds。
     4. 对于 input 动作，我们认为其含义是首先点击 bounds 对应的 element，接着对这个 element 进行文本输入。文本输入的内容是 message，需要收集者提供给脚本。
     5. 对于非 swipe 动作，脚本会自动将输入的动作执行。对于 swipe 动作，trace 收集者需要手动 swipe（因为输入滑动的起点和终点较复杂，且我们 groundtruth 的收集过程无需可复现）。
     6. 脚本跳转到 a.
   - 原则（如果有没提到的特殊情况，请在群中讨论或私聊冉德智/吴孟周）：
     1. 我们在收集过程中应当尽量避免弹窗（例如 app 索要权限，app 提醒网络不佳，app 赠送优惠券，广告）的情况（即如果遇到弹窗，应当手动关闭并不记录在 screenshot, hierarchy, action sequence 中），因为我们认为处理弹窗是 Agent 应当做到的。
     2. 我们在收集 trace 之前应当提前做好登录工作，这是因为我们认为 login 是包含在 Agent 之外的。
     3. 在选择交互的 element 时应尽量注意准确性。例如如果同一个位置有多个控件，对于 click 操作尽量选择 clickable=true 的控件，对于 text 操作尽量选择 class=android.widget.EditText 的控件。这一操作可以通过在 uiauto 的左侧屏幕上点击你要交互的控件，之后不断在右侧层级树上寻找儿子/兄弟/父亲关系等并观察中间 element 信息实现。
     4. ...
6. 第二步是收集 evaluator（详见脚本的 CollectEvaluator 函数）：
   - 定义：
     1. match_rules：为一个 dict，这个 dict 的 key 为 action 或者 element 的 attribute，value 为这个 attribute 具有的信息。具体过程见下一行。
     2. match_type：取值为 equal 或者 include。equal 的意思是要求二者相等，include 的意思是要求 attribute 含有 check_rules 的 value 中的信息。Evaluator 需要能够根据 match_rules 和 match_type 的指定定位到某个 element（对于 stoppage 类型的 evaluator）或者某个 action（对于 findaction 类型的 evaluator）。
     3. check_rules：为一个 dict，这个 dict 的 key 为 action 或者 element 的 attribute，value 为这个 attribute 具有的信息。具体过程见下一行。
     4. check_type：取值为 equal 或者 include。equal 的意思是要求二者相等，include 的意思是要求 attribute 含有 check_rules 的 value 中的信息。在 Evaluator 获取需要比较的 element/action 之后，需要能够根据 check_rules 和 check_type 的指定比较某些 attribute 是否合法。
   - 步骤：
     1. 输入 evaluator 类型：
        - stoppage：在最终页面（即 stop 动作对应的页面）中通过 match_type, match_rules 定位某个 element，并使用 check_type, check_rules 检验该 element 是否合法。比较特别的，check_rules 中可以使用 activity。
        - findaction：使用 match_type, match_rules 定位 action sequence 中的某个 action（注意这里既可以使用 action 的 attribute 如 action type 和 message，对于需要与 element 做交互的 action 也可以使用 element 的 attribute 如 resource-id, content-desc, class），并使用 check_type, check_rules 检验该 action 是否合法（同理，也可以使用 action 和 element 二者的 attribute）。
        - lastaction：检验最后一个动作是否满足一些条件，仅需要提供 check_type 和 check_rules。同上可以使用 action 和交互的 element 二者的 attribute。
        - findelement：使用 match_type, match_rules 定位 hierarchy sequence 中的某个 element，并使用 check_type, check_rules 检验该 element 是否合法。match_rules 和 check_rules 均可以使用 activity。
        - findelementbyaction：使用 action_match_type, action_match_rules 定位 action sequence 中的某个 action，并在该 action 对应的 hierarchy 上使用 element_match_type, element_match_rules 定位某个 element，使用 check_type, check_rules 检验该 element 是否合法。
     2. 按照脚本要求，根据不同的 evaluator 类型输入不同的参数。特别的，如果 check_type 和 match_type 为 equal，可以直接输入回车，脚本会自动选择 equal 类型。
     3. 输入参数后继续收集下一个 evaluator，如果没有 evaluator 直接输入回车结束 evaluator 收集。
     4. 在收集完所有 evaluator 后，脚本会自动使用你收集的 evaluator 对你收集的 trace 做评测，如果无法通过评测，你需要关注无法通过的原因是什么并根据脚本提示自行清除 trace 或者 evaluator 重新收集。
   - 原则（如果有没提到的特殊情况或你的 trace 无法使用现有的 evaluator 检验，请在群中讨论或私聊冉德智/吴孟周）：
     1. 对于一个 trace，不同 evaluator 之间的关系是 and，即如果一个 trace 能够通过多个 evaluator 才算成功。
     2. evaluator 的收集过程中应尽量避免 bounds 的使用，这是因为 bounds 是容易改变的。
     3. 如果对于一个 trace 有多个 evaluator，默认的实现是不考虑顺序的，也就是我们只关心整个序列中是否存在分别某个 action 或者某个 element，而不关心他们的先后，但可能一些情况下这些先后关系是重要的。如果你遇到了这种情况，我们还提供了类型为 "rule" 的 evaluator，具体来说你需要自行更改 evaluator.json 文件并在修改完成后重新跑一次脚本以检验更新后的 evaluator 是否正确。具体来说 rule 类型的 evaluator 要求传入一个 order 和一系列 evaluators，之后根据 order 类型检验 evaluators 是否合法。我们提供的 order 类型分别是 sequential, consecutive 和 present，其中 sequential 要求所有的 evaluator 在 trace 上进行顺序的匹配，consecutive 要求所有的 evaluator 在 trace 上匹配为一个连续（为了方便，我们将连续定义为既可以匹配 hierarchy 和后续的 action，也可以忽略中间的 hierarchy 匹配两个连续的 action）的子串，present 则与默认相同仅要求均出现。同时，我们支持 rule evaluator 进行嵌套。值得注意的是，我们在三种 order 类型中都允许了同一个 hierarchy 或者 action 被多个 evaluator 匹配，因此我们可以对同一个 hierarchy 或者 action 使用多个 evaluator。我们在 groundtruth/1/a53 文件夹下提供了 5 个可以通过测试的 evaluator 和 2 个不能通过测试的 evaluator 作为示范。
     4. ...
7. 第三步是收集 skills（详见脚本的 CollectSkill 函数）：
   - 定义：我们将一个 skill 定义为几个连续的 action，这些 action 应当共同完成一个初等的目标（例如将使用 tip calculator 计算 tip 的问题分解为“输入信息”和“点击计算按钮”）。
   - 步骤：脚本不断输出标注过的剩余的 action trace，skill 收集者不断输入 skill 的长度（从起始开始）和 skill 名称即可。
   - 原则（如果有没提到的特殊情况，请在群中讨论或私聊冉德智/吴孟周）：
     1. 在同一个task instruction在不同app的实现下，完成相同目标的skill应保持名字一致，使得我们后续能够进行自动匹配。
     2. ...

## 后续更新

如果在使用中发现任何 bug，可以直接修改后更新，也可以在群聊中告知我或私聊。
