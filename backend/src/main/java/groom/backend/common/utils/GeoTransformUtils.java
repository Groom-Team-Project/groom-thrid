package groom.backend.common.utils;


import org.locationtech.proj4j.*;

public class GeoTransformUtils {

  private static final CRSFactory crsFactory = new CRSFactory();
  private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();

  /**
   * EPSG 4326 좌표계 변환
   * @param lat 위도
   * @param lng 경도
   * @param EPSGCode 좌표계 예시 : "EPSG:5181"
   * @return Float[]{x, y} → 4326 좌표 (동북 방향 m 단위)
   */
  public static Double[] toEPSG4326(Double lat, Double lng, String crsCode) {

    CoordinateReferenceSystem srcCrs = crsFactory.createFromName(crsCode);
    CoordinateReferenceSystem dstCrs = crsFactory.createFromName("EPSG:4326");

    CoordinateTransform transform = ctFactory.createTransform(srcCrs, dstCrs);

    // Proj4J는 (lng, lat) 순서 사용
    ProjCoordinate src = new ProjCoordinate(lng, lat);
    ProjCoordinate dst = new ProjCoordinate();

    try {
      transform.transform(src, dst);
    } catch (Exception e) {
      throw new RuntimeException("EPSG:4326 좌표 변환 실패", e);
    }

    return new Double[]{(Double) dst.x, (Double) dst.y};
  }
}
