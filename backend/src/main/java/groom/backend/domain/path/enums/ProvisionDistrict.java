package groom.backend.domain.path.enums;
/**
 * Tmap API 서비스 제공 구역
 * 단위 : 시군구
 * Enum 명칭 참고 : 주소지 로마자 표기
 */
public enum ProvisionDistrict {
  // ProvisionCity에 대한 의존성을 가져야함.
  // TODO : 입력 검증을 위해, ProvisionCity에서 true로 되어있는 시도의 시군구 또한 enum으로 넣어줄 것.
  // TODO : 시도 시군구 추가시, 각 시도의 명칭을 KAKAO API 기준으로 등록할 것.
  // 현재는 구역 전체가 제공구역이 아닌 시도에 한해서만 enum에 등록할 것.
  // 제공되지 않는 시군구의 경우, INVALID로 통일

  // 경기도 - 3개 군을 제외한 28개 시 모두에서 지원
  SUWON("수원시", true),
  GOYANG("고양시", true),
  GWACHEON("과천시", true),
  GWANGMYEONG("광명시", true),
  GWANGJU("광주시", true),
  GURI("구리시", true),
  GUNPO("군포시", true),
  GIMPO("김포시", true),
  NAMYANGJU("남양주시", true),
  DONGDUCHEON("동두천시", true),
  BUCHEON("부천시", true),
  SEONGNAM("성남시", true),
  SIHEUNG("시흥시", true),
  ANSAN("안산시", true),
  ANSEONG("안성시", true),
  ANYANG("안양시", true),
  YANGJU("양주시", true),
  YEOJU("여주시", true),
  OSAN("오산시", true),
  YONGIN("용인시", true),
  UIWANG("의왕시", true),
  UIJEONGBU("의정부시", true),
  ICHEON("이천시", true),
  PAJU("파주시", true),
  PYEONGTAEK("평택시", true),
  POCHEON("포천시", true),
  HWASEONG("화성시", true),

  // 강원도
  CHUNCHEON("춘천시", true),
  GANNEUNG("강릉시", true),
  DONGHAE("동해시", true),
  SAMCHEOK("삼척시", true),
  SOKCHO("속초시", true),
  WONJU("원주시", true),
  TAEBAEK("태백시", true),

  // 충청북도
  CHEONGJU("청주시", true),
  CHUNGJU("충주시", true),
//  DANYANG("단양군"), 일부 지역

  // 충청남도
  CHEONAN("천안시", true),
  GONGJU("공주시", true),
  ASAN("아산시", true),
  NONSAN("논산시", true),

  // 전라북도 - 전북특별자치도
  JEONJU("전주시", true),
  GUNSAN("군산시", true),
  IKSAN("익산시", true),
  NAMWON("남원시", true),
  WANJU("완주군", true),

  // 전라남도
  MOKPO("목포시", true),
  YEOSU("여수시", true),
  SUNCHEON("순천시", true),

  // 경상북도
  GYEONGJU("경주시", true),
  GYEONGSAN("경산시", true),
  GUMI("구미시", true),
  ANDONG("안동시", true),
  POHANG("포항시", true),
//  YEONGDEOK("영덕군"), 일부 제공

  // 경상남도
  CHANGWON("창원시", true),
  GEOJE("거제시", true),
  GIMHAE("김해시", true),
  YANGSAN("양산시", true),
  JINJU("진주시", true),
  TONGYEONG("통영시", true),


  INVALID("미제공 구역 또는 유효하지 않은 값", false);

  private String name;
  private Boolean isAble;

  public static ProvisionDistrict findByName(String name) {
    for (ProvisionDistrict city : ProvisionDistrict.values()) {
      if (city.name.equals(name)) {
        return city;
      }
    }
    return INVALID;
  }

  private ProvisionDistrict(String name, Boolean isAble) {
    this.name = name;
    this.isAble = isAble;
  }

  public String getName() {return this.name;}
  public Boolean getIsAble() {return this.isAble;}
}
