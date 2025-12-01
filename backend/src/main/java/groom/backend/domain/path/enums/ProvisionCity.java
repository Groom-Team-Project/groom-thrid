package groom.backend.domain.path.enums;

/**
 * Tmap API 서비스 제공 구역
 * 단위 : 시도
 * Enum 명칭 참고 : 주소지 로마자 표기
 */
public enum ProvisionCity {
  // 서울 및 6개 광역시, 제주에 대해 모든 구역에서 서비스 제공
  SEOUL("서울특별시", true),
  BUSAN("부산광역시", true),
  DAEGU("대구광역시", true),
  INCHEON("인천광역시", true),
  GWANGJU("광주광역시", true),
  DAEJEON("대전광역시", true),
  ULSAN("울산광역시", true),
  JEJU("제주도", true),
  INVALID("없음", false);

  private String name;
  private Boolean isAble;

  public static ProvisionCity findByName(String name) {
    for (ProvisionCity city : ProvisionCity.values()) {
      if (city.name.equals(name)) {
        return city;
      }
    }
    return INVALID;
  }

  private ProvisionCity(String name, Boolean isAble) {
    this.name = name;
    this.isAble = isAble;
  }

  public String getName() {return name;}
  public Boolean getIsAble() {return this.isAble;}
}
