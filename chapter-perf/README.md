Performance tests
=================

These performance test are dependent on the machine the test are run. Some test are designed to fail, due to
performance problems, as described in the book. These test are:
* aia.performance.dispatcher.DispatcherInitTest
* aia.performance.dispatcher.DispatcherPinnedTest
* aia.performance.dispatcher.DispatcherSeparateTest
* aia.performance.dispatcher.DispatcherThroughputTest

But it is possible that other tests fail too. Especially when running all the test at once.
These are designed to stress the system its running on. Therefore these test can fail on some systems
and succeed on others. These test are more to get the feeling for the effects the different configurations
can have on the performance of an application


성능테스트
==========

일부 테스트는 기계에 따라 실패할 수도 있다. 일부 테스트는 책에 설명한데로, 성능 문제로 인해 원래 실패하도록 
설계된 것이다. 그런 테스트는 다음과 같다.

* aia.performance.dispatcher.DispatcherInitTest
* aia.performance.dispatcher.DispatcherPinnedTest
* aia.performance.dispatcher.DispatcherSeparateTest
* aia.performance.dispatcher.DispatcherThroughputTest

하지만 다른 테스트도 실패할 수 있다. 특히 모든 테스트를 동시에 실행하는 경우 더 그렇다. 각 테스트는 실행시 시스템에 
스트레스를 주도록 고안된 것이다. 따라서 그런 테스트는 일부 시스템에서는 성공하지만 일부 시스템에서는 실패할 수 있다. 
그런 테스트가 있는 이유는 여러가지 설정이 애플리케이션의 성능에 끼치는 영향을 여러분이 느낄 수 있게 하기 위함이다.