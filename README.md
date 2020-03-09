## 一个适用于Android的三方SDK初始化工具
#### 解决了三方sdk初始化代码不优雅的问题，同时通过线程池初始化提高了应用启动速度

## Features
* 可以指定任务执行的线程，分为主线程、CPU线程和IO线程
* 可以指定任务的依赖关系，解决了一个库需要依赖其他库启动的问题
* 可以在主线程空闲时进行延迟初始化
* 支持某些任务放到线程池执行，但是又必须在onCreate中初始化完成

## Usages
```java
    // 1、创建初始化任务的Task，通过复写方法指定线程、依赖等，具体配置参见ITask接口
    // 同时提供了TaskBuilder快速配置Task
    public class TaskA extends Task {
        @Override
        public ThreadMode threadMode() {
            return ThreadMode.MAIN;
        }

        @Override
        public List<Class<? extends Task>> dependsOn() {
            return listOf(
                    TaskB.class,
                    TaskC.class,
                    TaskD.class
            );
        }

        @Override
        public void run() {
            Log.e("task_", getClass().getSimpleName() + "在" + Thread.currentThread().getName() + "  执行了");
        }
    }

    // 2、开始执行
    Initiator.init(this);
    Initiator.create()
        .addTask(new TaskA())
        .addTask(new TaskE())
        .addTask(new TaskC())
        .addTask(new TaskD())
        .addTask(new TaskB())
        .start();

    // 3、如提交的任务有必须在onCreate生命周期内完成的
    Initiator.await();
```

#### 参考 [MyPerformance](https://github.com/dicallc/MyPerformance)