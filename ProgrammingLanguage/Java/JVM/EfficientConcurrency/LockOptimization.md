### 锁优化

> 高效并发时从JDK1.5到JDK1.6的一个重要改进

HotSpot虚拟机开发团队，在这个版本上话费了大量的经历去实现各种锁优化技术：
1. 适应性自旋(Adaptive Spinning)
2. 锁消除(Lock Elimination)
3. 锁粗化(Lock Coarsening)
4. 轻量级锁(Lightweight Locking)
5. 偏向锁(Biased Locking)

[详细见《深入理解Java虚拟机》13.3节]()