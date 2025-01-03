# flink


## Watermarks

watermarks 的作用 — 它们定义何时停止等待较早的事件。

Flink 中事件时间的处理取决于 watermark 生成器，后者将带有时间戳的特殊元素插入流中形成 watermarks。

事件时间 t 的 watermark 代表 t 之前（很可能）都已经到达。

当 watermark 以 2 或更大的时间戳到达时，事件流的排序器应停止等待，并输出 2 作为已经排序好的流。

延迟是相对于 watermarks 定义的。Watermark(t) 表示事件流的时间已经到达了 t; watermark 之后的时间戳 ≤ t 的任何事件都被称之为延迟事件。

```java
DataStream<Event> stream = ...

WatermarkStrategy<Event> strategy = WatermarkStrategy
        .<Event>forBoundedOutOfOrderness(Duration.ofSeconds(20))
        .withTimestampAssigner((event, timestamp) -> event.timestamp);

DataStream<Event> withTimestampsAndWatermarks =
    stream.assignTimestampsAndWatermarks(strategy);
```

使用最大无序边界 (bounded-out-of-orderness) watermark 策略生成器，定义了等待时间最多 20s。只有那些超过最大无序边界的事件才会被丢弃

## Windows

### 窗口分配器

**滚动时间窗口**

**滑动时间窗口**

**会话窗口**

### 窗口应用函数

我们有三种最基本的操作窗口内的事件的选项:

1. 像批量处理，ProcessWindowFunction 会缓存 Iterable 和窗口内容，供接下来全量计算；
2. 或者像流处理，每一次有事件被分配到窗口时，都会调用 ReduceFunction 或者 AggregateFunction 来增量计算；
3. 或者结合两者，通过 ReduceFunction 或者 AggregateFunction 预聚合的增量计算结果在触发窗口时， 提供给 ProcessWindowFunction 做全量计算。

