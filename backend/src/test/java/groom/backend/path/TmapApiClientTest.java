package groom.backend.path;

import groom.backend.domain.path.dto.request.PathFindRequest;
import groom.backend.domain.path.dto.response.PathFindResponse;
import groom.backend.domain.path.service.spec.TmapApiClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TmapApiClientTest {

  @Autowired
  private TmapApiClient tmapApiClient;

  @Test
  void testTmapApi() {
    PathFindRequest req = new PathFindRequest(
            126.547121,
            34.897521,
            126.549669,
            34.897907,
            "따흐흑",
            "따흐앙"
    );

    PathFindResponse res = tmapApiClient.tmapApiPathFind(req);
    Assertions.assertNotNull(res);
    System.out.println(res);
  }
}
