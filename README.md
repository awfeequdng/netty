[toc]

### netty概述

- [netty](netty.io)是一个**异步的基于事件驱动的网络应用框架**，用于快速开发可维护的高性能协议服务端和客户端。

#### 设计

- 针对各种传输类型设计了一个统一的API，无论是阻塞的还是非阻塞的socket。

- 基于一种灵活的并且可扩展的事件模型，可进行关注点分离

- 提供了高度可定制化的线程模型，单线程、一个或多个线程池等，比如`SEDA`

  > SEDA：Staged Event Driven Architecture，把一个请求处理过程分成若干Stage，不同的Stage使用不同数量的线程来处理

- 可以真正实现无连接的数据报支持

  ![](img/netty_概述.png)

### BIO

- ![](img/BIO_chain.png)

#### 装饰者模式

- 装饰者模式是继承关系的一个替代方案，**可以在不创建更多子类，不必改变原类文件的情况下，通过创建一个包装对象，动态扩展对象功能**
- 装饰者模式用来扩展特性对象的功能，不需要子类，是动态扩展，运行时分配职责，可以防止由于子类而导致的复杂和混乱，有更多的灵活性，对于一个给定的对象，同时可能有不同的装饰对象，客户端可以通过它的需要选择合适的装饰对象发消息
- 继承用来扩展一类对象的功能，需要子类，是静态的，编译时分配职责，导致很多子类产生，确认灵活性

### NIO

- BIO中最为核心的概念是流(Stream)，面向流编程，一个流要么是输入流，要么是输出流
- NIO核心：通道、缓冲区、选择器。NIO中是面向块(block)或是缓冲区(buffer)编程的
- Buffer本身就是一块内存，实际是个数组，数据的读写都是通过buffer来实现
- Channel值得是可以向其写入数据或者从中读取数据的对象
- Channel是双向的，一个流只可能是输入流或者输出流，但Channel打开后可以读、写

### java.nio.Buffer

> 一个具体的原生类型的数据容器
>
> Buffer是一个线性的，特定原生类型元素的有限序列，除了它的内容之外，一个buffer重要的属性就是`capacity`，`limit`，`position`
>
> 一个buffer的`capacity`就是它所包含的元素数量，一个buffer的`capacity`不会为负数并且不会变化
>
> 一个buffer的`limit`指第一个不应该被读或写的元素索引，一个buffer的`limit`不会为负数并且不会大于`capacity`
>
> 一个buffer的`position`指下一个将被读或写的元素索引。一个buffer的`position`不会为负数并且不会大于`limit`

- 比如创建一个大小为`10`的`ByteBuffer`对象，初始时`position=0`，`limit`和`capacity`为`10`

  ![](img/buffer_1.png)

- 调用`buffer.put()`方法或`channel.read()`方法向`buffer`输入4个字节数据后，`position`指向4，即下一个操作的字节索引为4

  ![](img/buffer_2.png)

- 如果从`buffer`输出数据，在此之前**必须调用`flip()`方法**，它将`limit`设为`position`当前位置，将`position`设为`0`

  ![](img/buffer_3.png)

- 调用`buffer.get()`方法或者`channel.write()`把让`buffer`输出数据后，`position`增加，`limit`不变，但`position`不会超过`limit`。把`4`字节数据都输出后，`position`、`limit`都指向4
- 比如创建一个大小为10的ByteBuffer对象，初始时position=0，limit和capacity为10

  ![](img/buffer_1.png)

- 调用`put()`方法从通道中读4个字节数据到缓冲区后，position指向4，即下一个操作的字节索引为4

  ![](img/buffer_2.png)

- 再从缓冲区把数据输出到通道，在此之前**必须调用`flip()`方法**，它将limit设为position当前位置，将position设为0

  ![](img/buffer_3.png)

- 调用`get()`方法把数据从缓冲区输出到通道，position增加，limit不变，但position不会超过limit。把4字节数据都输出后

  ![](img/buffer_4.png)

- 调用`clear()`方法，指针又变为原状态

  ![](img/buffer_5.png)

> 对于每个非布尔类型的原生类型，这个类都有一个子类
>
> 每个子类都定义了两类`get`和`put`操作
>
> 相对操作：从当前position开始根据传入的元素个数增加position位置，读或写一个或多个元素。如果所要求的转换超过了limit大小，那么相对的get操作会抛出`BufferUnderflowException`异常，相对的put操作会抛出`BufferOverflowException`异常，无论哪种情况，都没有数据传输
>
> 绝对操作：接收一个显示的元素索引，不会影响position，如果操作的元素索引超出了limit大小，那么get或put操作会抛出`IndexOutOfBoundsException`异常
>
> 数据还可以通过恰当的IO通道这种操作来输入或者从buffer输出，它总是相对于当前的position。
>
> 一个buffer的mark表示当`reset()`方法被调用时它的position会被重置到那个索引位置。如果mark定义了，当position或者limit调节到比mark小的值时，mark会被丢弃。如果mark没有定义，但调用了`reset()`方法，就会抛出`InvalidMarkException`异常。

- **`0 <= mark <= position <= limit <= capacity`**

> 一个新创建的buffer，它的position值总是为0，mark值是未定义的。limit值可能是0，也可能是其他值，取决于buffer构建的方式，新分配的buffer它的每个值都是0
>
> 除了访问position，limit，capacity值及重置mark值之外，这个类还定义了如下操作
>
> `clear()`让一个buffer准备好一个新的通道的读或者相对的`put()`操作，它会将limit设为capacity的值，将position设为0。
>
> `flip()`让一个buffer准备好一个新的通道的写或者相对的`get()`操作，它会将limit设为position的值，将position设为0。
>
> `rewind()`让一个buffer准备好重新读它已经包含的数据，它会将limit保持不变，将position设置为0。
>
> 每个buffer都是可读的，但不是每个buffer都是可写的，每个buffer的可变方法都被指定为可选操作。如果在只读buffer上调用写方法会抛出`ReadOnlyBufferException`异常。只读buffer不允许内容发生改变，但它的mark、position、limit值是可以变化的，无论一个buffer只读与否，都可以通过`isReadOnly()`方法来判断。
>
> buffer不是线程安全的，如果buffer在多线程中使用，需要进行同步操作
>
> 这个类中的方法，如果没有指定返回值，那么会返回这个buffer本身。这就允许方法可以链接起来，比如：`b.flip();b.position(23);b.limit(42)`可以被`b.flip().position(23).limit(42)`替代。

#### 零拷贝

- 创建`ByteBuffer`时，可以用`ByteBuffer.allocate(1024)`创建指定大小的`HeapByteBuffer`对象，`HeapByteBuffer`也即在堆中分配的`ByteBuffer`；

  也可以用`ByteBuffer.allocateDirect(1024)`来创建，它创建的是`DirectByteBuffer`对象

  ```java
      DirectByteBuffer(int cap) {                   
          super(-1, 0, cap, cap);
          boolean pa = VM.isDirectMemoryPageAligned();
          int ps = Bits.pageSize();
          long size = Math.max(1L, (long)cap + (pa ? ps : 0));
          Bits.reserveMemory(size, cap);
  
          long base = 0;
          try {
              //DirectByteBuffer由unsafe对象调用native方法来分配
              base = unsafe.allocateMemory(size);
          } catch (OutOfMemoryError x) {
              Bits.unreserveMemory(size, cap);
              throw x;
          }
          unsafe.setMemory(base, size, (byte) 0);
          if (pa && (base % ps != 0)) {
              // Round up to page boundary
              address = base + ps - (base & (ps - 1));
          } else {
              address = base;
          }
          cleaner = Cleaner.create(this, new Deallocator(base, size, cap));
          att = null;
      }
  ```

  `DirectByteBuffer`由`unsafe`对象调用`native`方法`allocateMemory`在堆外(操作系统中)分配。

  由`Deallocator`释放，`Deallocator`是`DirectByteBuffer`的静态私有内部类，实现了`Runnable`方法，其`run`方法中通过`unsafe.freeMemory(address)`释放。

- 在它们的父类`java.io.Buffer`中，有如下`address`字段，这个`address`只会被`direct buffer`所使用，之所以把它升级放在了`java.io.Buffer`中，是为了提升`JNI GetDirectBufferAddress`的速率。当使用`DirectByteBuffer`时由`address`来操作堆外内存，保证不会内存泄露。

  ```java
  	// Used only by direct buffers
      // NOTE: hoisted here for speed in JNI GetDirectBufferAddress
      long address;
  ```

- 在堆中分配的`HeapByteBuffer`，它的字节数组是在java堆上分配的，但进行IO操作时，操作系统并不直接处理堆上的字节数组，而是在操作系统上开辟一块内存区域，将堆上字节数组的数据拷贝到该内存区域，然后该内存区域与IO设备进行交互

- 如果使用`DirectByteBuffer`，就不会在堆上分配数组，而是直接在操作系统中分配。就少了一次数据拷贝的过程。

- 为何操作系统不直接访问java堆上的数据，而要拷贝到堆外：操作系统在内核态是可以访问任何一块内存区域的，如果访问堆，会通过JNI方式来访问，内存地址确定了，才能根据地址访问。但如果访问时，发生了GC，可能碰到标记-压缩，压缩后对象地址就发生了变化，再访问就出问题了。如果不GC，有可能会发生`OutOfMemoryError`。如果让对象位置不移动，也不可行。所以不能直接访问java堆上的数据

- 进行IO操作时，IO速度相对较慢，将堆上数据拷贝到操作系统相对较快(JVM保证拷贝时不会GC)，所以是可行的。

### java.nio.MappedByteBuffer

- 是`java.nio.ByteBuffer`的子类，`java.nio.ByteBuffer`又是`java.nio.Buffer`的子类
- 是一种允许程序直接从内存访问的特殊文件，可以将文件映射到内存中，由操作系统负责将内存修改写入到文件中，我们只需要处理内存的数据。用于内存映射的文件内存本身是在堆外，

> 一个`direct byte buffer`，内容是一个文件的内存映射区域(`memory-mapped region`)
>
> `MappedByteBuffer`可以通过`java.nio.channels.FileChannel.map()`创建
>
> 一个`mapped byte buffer`以及它所代表的文件映射在`buffer`本身被垃圾回收前一直有效
>
> `mapped byte buffer`的内容随时可以更改。

### java.nio.channels.Selector

> 是一个双工的`java.nio.channels.SelectableChannel`对象
>
> `selector`可以通过调用这个类的`open()`方法来创建，`open()`会使用系统默认的`java.nio.channels.spi.SelectorProvider`选择器提供者来创建新的`selector`，`selector`也可以通过一个自定义的`selector`提供者调用`java.nio.channels.spi.SelectorProvider.openSelector()`方法来创建，一个`selector`在调用它的`close()`方法之前会一直处于`open`状态
>
> 一个可选择的`channel`注册到`selector`上是通过`SelectionKey`对象来表示的，一个`selector`维护三个`selection key`集合：
>
> - `key set`：它包含的`key`表示当前`selector`上注册的`channel`，这个集合通过`keys()`方法返回
> - `selected-key set`：这个集合通过`selectedKeys()`方法返回，它永远是`key set`的一个子集
> - `cancelled-key  set`：它表示`key`已经被取消了，但`channel`还没被取消，这个集合不能直接访问。`cancelled-key set`也永远是`key set`的一个子集。
>
> 在一个新的`selector`新创建时，这三个集合都是空的
>
> 一个`key`被添加到`selector`的`key set`中是作为通过`SelectableChannel.register(Selector,int)`方法注册一个`channel`的副作用(就是说注册`channel`就会把`key`添加到`selector`中)，在`selection`操作中`cancelled key`会从`key set`中移除。`key set`本身是不能直接被修改的。
>
> 一个`key`当它被取消时，它会被添加到它的`selector`的`cancelled-key set`中，无论是通过关闭它的`channel`还是调用`SelectionKey.cancel()`方法。取消一个`key`会导致在下次`selection`操作中它的`channel`会被取消注册，此时这个`key`会从`selector`的所有`key`集合中移除掉。
>
> 通过`selection`操作，`key`会被添加到`selected-key set`中，一个`key`可能通过调用这个`set`的`java.util.Set.remove(java.lang.Object)`方法或者根据这个`set`获得的`iterator`的`java.util.Iterator.remove()`方法直接从`set`中移除。`key`不会以任何其他的方式从`selected-key set`中移除。
>
> 在每一个`selection`操作中，`key`可能被添加到一个`selector`的`selected-key set`中或者从`selector`的`selected-key set`中移除，也可能从`cancelled-key set`中移除，`selection`操作是通过`select()`、`select(long)`、`selectNow()`等方法来执行的，分为三个步骤：
>
> - 在`cancelled-key set`中的每个`key`都会从`set`中被移除掉，它的`channel`会被取消注册，这个步骤使得`cancelled-key set`变为空集合
>
> - 在`selection`操作开始时，底层的操作系统会查询是否更新未被取消注册的`channel`的准备状况，执行任何我们的感兴趣的操作，对于准备有至少一个操作的`channel`，如下两个动作会被执行：
>
>   - 如果`channel`的`key`不在`selected-key set`中，它会被添加到`set`中，它的`ready-operation set`会被修改以精确地表示`channel`已经报告准备好的这些`operation`，任何之前记录的准备信息会被丢弃掉。
>   - 如果`channel`的`key`已在`selected-key set`中，它的`ready-operation set`会被修改以便标识`channel`已经报告准备好的任何新的操作。之前所记录的准备信息会被保留下来，换句话说，由操作系统返回的准备集合会按位的方式放到`key`的当前的准备集合中。
>
>   如果`key set`中所有的`key`在这个步骤开始时没有感兴趣的集合，那么`selected-key  set`或者任何`key`的`ready-operation`都不会被更新
>
> - 如果在步骤2中有任何`key`被添加到`cancelled-key set`中，那么它们会在步骤1中被处理
>
> 无论一个`selection`操作是否阻塞的等待一个或多个通道变得可用，如果是，等待多长时间，这是这三个`selection`方法的本质区别。
>
> 一个`selector`的`key`和`selected-key set`在多线程并发中使用通常不是安全的，如果一个线程直接修改集合中的一个，那么这个访问要通过`set`本身做一个同步。由`set`的`java.util.Set.iterator()`方法所返回的`iterator`是快速失败的，如果在`iterator`创建后`set`被修改了，处理调用`iterator`自己的`java.util.Iterator.remove()`方法之外，任何其他修改都会导致`java.util.ConcurrentModificationException`

#### java.nio.channels.Selector.open()

> 打开一个选择器
>
> 新的`selector`是系统范围内的`java.nio.channels.spi.SelectorProvider`对象通过调用`java.nio.channels.spi.SelectorProvider.openSelector()`来创建，

### java.nio.channels.SelectionKey

> 一个代表`java.nio.channels.SelectableChannel`注册到`java.nio.channels.Selector`的`token`。
>
> 每次一个`channel`注册到`selector`上`selection key`都会被创建，在调用它的`cancel()`方法或者关闭它的`channel`之前，这个`key`都有效。取消一个`key`，不会立马从它的`selector`中移除，而是在下次进行`selection`操作时添加到`cancelled-key set`进行移除。`key`的有效性可以通过`isValid()`方法检测
>
> 一个`selecton key`包含两个用整数表示的操作集合，每组操作集合表示`key`的`channel`支持的`selectable`操作的分类。
>
> - `ready set`/`interest set`

### 零拷贝

- 传统方式

  1. 用户向`kernel`发送`read()`系统调用，切换到内核空间，`kernel`通过`DMA(direct memeory access`)方式将数据从硬盘拷贝到`kernel buffer`中
  2. 将`kernel buffer`数据拷贝到`user buffer`中，切换到用户空间
  3. 执行业务逻辑
  4. 用户向`kernel`发送`write()`系统调用，切换到内核空间，将`user buffer`数据拷贝到`kernel buffer`中，将`kernel buffer`数据拷贝到`kernel socket buffer`，该数据写到网络中后，`write()`返回，切换到用户空间

  - 四次切换、四次数据拷贝

  <img src="img/zero_copy_1.png" style="zoom:80%;" />

- 

  1. 用户向`kernel`发送`sendfile()`系统调用，切换到内核模式，`kernel`通过`DMA(direct memeory access`)方式将数据从硬盘拷贝到`kernel buffer`中
  2. `kernel`将数据写到`target socket buffer`中，从其中发送数据
  3. 发送完后`sendfile()`返回

  - 没有内核空间与用户空间之间的数据拷贝，但内核空间中存在数据拷贝(`kernel buffer --> target socket buffer`)
  - 两次数据拷贝

  <img src="img/zero_copy_2.png" style="zoom:80%;" />

- 

  1. 用户向`kernel`发送`sendfile()`系统调用，切换到内核模式，`kernel`通过`DMA(direct memeory access`)方式将数据从硬盘拷贝到`kernel buffer`中，还可以通过`scatter/gather DMA`方式将数据读取到`kernel buffer`中

  <img src="img/zero_copy_3.png" style="zoom: 80%;" />

- 零拷贝

  1. 发送`sendfile`系统调用前处于用户空间；发送`sendfile`系统调用后处于内核空间；执行完后又回到用户空间

  2. **将磁盘数据拷贝到`kernel buffer`中，再将`kernel buffer`中数据的`fd(文件描述符)`拷贝到`socket buffer`中，`fd`包含：数据的内存地址、数据的长度。不再需要将数据拷贝到`socket buffer`中了。**

  3. **`protocol engine(协议引擎)`完成数据发送时，从两个`buffer`中读取信息。即`gather(收集)`操作**

     ![](img/zero_copy_4.png)

### 源码分析



#### EventExecutorGroup

> `EventExecutorGroup`通过它的`next()`方法提供`io.netty.util.concurrent.EventExecutor`进行使用，除此之外，还负责它们的生命周期以及对它们以全局的方式进行关闭

- EventExecutor next()

> 返回由`EventExecutorGroup`所管理的`io.netty.util.concurrent.EventExecutor`

#### EventLoopGroup

- 继承自`EventExecutorGroup`接口

> 是一个特殊的`io.netty.util.concurrent.EventExecutorGroup`，在进行事件循环的过程中，在选择操作时允许注册`channel`

- EventLoop next()

> 返回下一个要使用的`io.netty.channel.EventLoop`

- ChannelFuture register(Channel channel)

> 将一个`channel`注册到`io.netty.channel.EventLoop`中，当注册完成时返回的`io.netty.channel.ChannelFuture`对象会收到通知。

- ChannelFuture register(ChannelPromise promise)

> 使用一个`io.netty.channel.ChannelFuture`将一个`channel`注册到`io.netty.channel.EventLoop`中，当注册完成时传入的`io.netty.channel.ChannelFuture`会收到通知并进行返回。

#### NioEventLoopGroup

继承`MultithreadEventLoopGroup`类，`MultithreadEventLoopGroup`类实现了`EventLoopGroup`接口

> 是`io.netty.channel.MultithreadEventLoopGroup`的一个实现，它用于基于`io.netty.channel.Channel`的NIO`java.nio.channels.Selector`对象

```java
	/**
	 * 使用默认的线程数、默认的java.util.concurrent.ThreadFactory，以及
	 * java.nio.channels.spi.SelectorProvider的provider()方法返回的SelectorProvider创建新的实
	 * 例
	 */
	public NioEventLoopGroup() {
        this(0);
    }

	/**
	 * 使用指定的线程数、java.util.concurrent.ThreadFactory以及java.nio.channels.spi.
	 * SelectorProvider的provider()方法返回的SelectorProvider创建新的实例
	 *
	 */
	public NioEventLoopGroup(int nThreads) {
        this(nThreads, (Executor) null);
    }

	// ...
    
	// 构造方法中nThreads的取值：nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads
	// 其父类MultithreadEventLoopGroup中设置DEFAULT_EVENT_LOOP_THREADS如下：
	private static final int DEFAULT_EVENT_LOOP_THREADS;
    static {
        // 如果没有配置io.netty.eventLoopThreads，就取 系统核数 * 2，否则取配置的数量，最小取1
        DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt(
                "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
    }
	// 最终会执行MultithreadEventExecutorGroup抽象类中的构造方法，
	// NioEventLoopGroup继承自MultithreadEventLoopGroup抽象类，
	// MultithreadEventLoopGroup继承自MultithreadEventExecutorGroup抽象类，
	// 该方法中根据nThreads生成EventExecutor children = new EventExecutor[nThreads]数组
	// 对于数组每个元素，通过children[i] = newChild(executor, args)进行赋值
	/** 
	 * MultithreadEventExecutorGroup中的newChild方法
	 * 创建一个新的EventExecutor，后续可通过next()方法进行访问它，这个方法将被服务与
	 * MultithreadEventExecutorGroup的每个线程所调用
	 */
	abstract EventExecutor newChild(Executor executor, Object... args) throws Exception
```

#### ServerBootstrap

> `io.netty.bootstrap.Bootstrap`的一个子类，使得我们可以轻松的启动`io.netty.channel.ServerChannel`
>
> `ServerChannel`：一个标记接口（和`java.io.Serializable`一样），会接收另外一端发过来的连接请求，并通过接收它们来创建`child`   `io.netty.channel.Channel`，例如`io.netty.channel.socket.ServerSocketChannel`。

- group(EventLoopGroup parentGroup, EventLoopGroup childGroup)

> 给`parent(acceptor)`和`child(client)`设置`EventLoopGroup`，这些`EventLoopGroup`用于处理`ServerChannel`和`Channel`的所有事件和IO
>
> ```java
> /** 这个方法的作用就是赋值bossGroup,workerGroup，也可以说parentGroup和childGroup */
> public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup) {
>     super.group(parentGroup);
>     if (childGroup == null) {
>         throw new NullPointerException("childGroup");
>     }
>     if (this.childGroup != null) {
>         throw new IllegalStateException("childGroup set already");
>     }
>     this.childGroup = childGroup;
>     return this;
> }
> ```

- channel(Class<? extends C> channelClass)

> 根据Class对象，创建对应的`io.netty.channel.Channel`实例。如果你的`Channel`没有无参的构造方法，要么使用这个，要么使用`channelFactory(io.netty.channel.ChannelFactory)`
>
> ```java
> public B channel(Class<? extends C> channelClass) {
>     if (channelClass == null) {
>         throw new NullPointerException("channelClass");
>     }
>     return channelFactory(new ReflectiveChannelFactory<C>(channelClass));
> }
> /** 
>  * ReflectiveChannelFactory实现了ChannelFactory，它会以反射的方式通过调用默认构造方法实例化一
>  * 个新的Channel对象
>  */
> ```

