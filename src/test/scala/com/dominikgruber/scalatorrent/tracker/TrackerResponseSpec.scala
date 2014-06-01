package com.dominikgruber.scalatorrent.tracker

import com.dominikgruber.scalatorrent.UnitSpec

class TrackerResponseSpec extends UnitSpec {

  "TrackerResponse" should "parse a correct response" in {
    val str = "d8:completei1e10:downloadedi0e10:incompletei0e8:intervali1625e5:peersld2:ip8:10.0.0.27:peer id20:-<30000-bitlove.org/4:porti6881eeee"
    TrackerResponse.create(str) should be (TrackerResponseWithSuccess(1625, None, None, 1, 0, List(Peer(Some("-<30000-bitlove.org/"), "10.0.0.2", 6881)), None))
  }

  it should "parse a response with a failure" in {
    val str = "d14:failure reason17:incorrect requeste"
    TrackerResponse.create(str) should be (TrackerResponseWithFailure("incorrect request"))
  }
}
