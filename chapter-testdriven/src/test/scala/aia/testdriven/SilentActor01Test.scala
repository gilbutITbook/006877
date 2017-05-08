package aia.testdriven

import org.scalatest.{WordSpecLike, MustMatchers}
import akka.testkit.TestKit
import akka.actor._

// 이 테스트는 책에는 포함되지 않음. defaultExcludedNames에 이 테스트가 들어있음

class SilentActor01Test extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {
  // 여기 있는 코드가 원래 책에 있는 코드임. travis CI시 컴파일이 되도록 
  // 변경함(테스트에서 오류 발생하면 CI가 작동 안할 것임)
  // "A Silent Actor" must {
  //   "change state when it receives a message, single threaded" in {
  //     // 테스트를 작성한다. 처음에 실패한다.
  //     fail("not implemented yet")
  //   }
  //   "change state when it receives a message, multi-threaded" in {
  //     // 테스트를 작성한다. 처음에 실패한다.
  //     fail("not implemented yet")
  //   }
  // }
  "A Silent Actor" must {
    "change state when it receives a message, single threaded" ignore {
      // 테스트를 작성한다. 처음에 실패한다.
      fail("not implemented yet")
    }
    "change state when it receives a message, multi-threaded" ignore {
      // 테스트를 작성한다. 처음에 실패한다.
      fail("not implemented yet")
    }
  }

}



class SilentActor extends Actor {
  def receive = {
    case msg =>
  }
}

