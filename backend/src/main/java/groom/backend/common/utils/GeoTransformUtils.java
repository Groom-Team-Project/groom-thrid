package groom.backend.common.utils;


import org.locationtech.proj4j.*;

public class GeoTransformUtils {

  private static final CRSFactory crsFactory = new CRSFactory();
  private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();

  /**
   * EPSG 5186 좌표계 변환
   * @param lat 위도
   * @param lng 경도
   * @param EPSGCode 좌표계 예시 : "EPSG:4236"
   * @return Float[]{x, y} → 5186 좌표 (동북 방향 m 단위)
   */
  public static Float[] toEPSG5186(float lat, float lng, String EPSGCode) {

    CoordinateReferenceSystem srcCrs = crsFactory.createFromName(EPSGCode);
    CoordinateReferenceSystem dstCrs = crsFactory.createFromName("EPSG:5186");

    CoordinateTransform transform = ctFactory.createTransform(srcCrs, dstCrs);

    // Proj4J는 (lng, lat) 순서 사용
    ProjCoordinate src = new ProjCoordinate(lng, lat);
    ProjCoordinate dst = new ProjCoordinate();

    try {
      transform.transform(src, dst);
    } catch (Exception e) {
      throw new RuntimeException("EPSG:5186 좌표 변환 실패", e);
    }

    // EPSG:5186 결과는 x, y (동/북) 순서. 변환 후 지리 좌표계가 아닌 투영 좌표계 사용
    return new Float[]{(float) dst.x, (float) dst.y};
  }
}
