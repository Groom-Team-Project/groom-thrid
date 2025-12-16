package groom.backend.domain.opendata.enums;

public enum FacilityType {
    ISOLATION_HOSPITAL("격리병원", false),
    SENIOR_CENTER("경로당", false),
    HIGH_SCHOOL("고등학교", false),
    PUBLIC_LIBRARY("공공도서관", false),
    PERFORMANCE_HALL("공연장", false),
    FACTORY("공장", false),
    PUBLIC_TOILET("공중화장실", true),
    TOURIST_ACCOMMODATION("관광숙박시설", false),
    SPECTATOR_VENUE("관람장", false),
    PRISON_DETENTION_CENTER("교도소·구치소", false),
    EDUCATION_TRAINING_CENTER("교육원(연수원등)·직업훈련소·학원(자동차학원, 무도학원 제외) 등", false),
    GOVERNMENT_OFFICE("국가 또는 지자체 청사", false),
    NATIONAL_HEALTH_INSURANCE_CORP("국민건강보험공단 및 지사", false),
    NATIONAL_PENSION_CORP("국민연금공단 및 지사", false),
    WORKERS_WELFARE_CORP("근로복지공단 및 지사", false),
    FINANCIAL_BUSINESS("금융업소 등 일반업무시설", false),
    DORMITORY("기숙사", false),
    ELDERLY_WELFARE_FACILITY("노인복지시설", false),
    MULTI_FAMILY_HOUSING("다세대주택", false),
    SHELTER("대피소", false),
    UNIVERSITY("대학교", false),
    HYPERMARKET("대형마트", false),
    WHOLESALE_RETAIL_MARKET("도매시정·소매시장" , false),
    LIBRARY("도서관", false),
    CITY_PARK("도시공원", false),
    ZOO_AND_BOTANIC("동식물원", false),
    BATHHOUSE("목욕장", false),
    BROADCAST_STATION("방송국", false),
    HOSPITALS("병원·치과병원·한방병원·정신병원·요양병원", false),
    PUBLIC_HEALTH_CENTER("보건소", false),
    MEMORIAL_HALL("봉안당(종교시설에 해당하는 것은 제외)", false),
    COMMUNITY_TRAINING_FACILITY("생활권수련시설", false),
    RESIDENTIAL_ACCOMMODATION("생활숙박시설", false),
    SWIMMING_POOL("수영장", false),
    SUPERMARKET_RETAIL("수퍼마켓·일용품 등의 소매점", false),
    CHILD_WELFARE_FACILITY("아동복지시설", false),
    APARTMENT("아파트", false),
    APARTMENT_AMENITY("아파트 부대복리시설", false),
    MASSAGE_PARLOR("안마시술소", false),
    OUTDOOR_PERFORMANCE_VENUE("야외음악당·야외극장·어린이회관", false),
    CHILD_CARE_CENTER("어린이집", false),
    TOWNHOUSE("연립주택", false),
    POST_OFFICE("우체국", false),
    SPORTS_COMPLEX("운동장(육상·구기·볼링·수영·스케이트·롤러스케이트·승마·사격·궁도·골프)과 부수되는 건축물", false),
    DRIVING_SCHOOL("운전학원", false),
    KINDERGARTEN("유치원", false),
    CLINICS("의원·치과의원·한의원·조산소·산후조리원", false),
    OTHER_SOCIAL_WELFARE_FACILITY("이외 사회복지시설" , false),
    BARBER_BEAUTY_SALON("이용원·미용원", false),
    GENERAL_LODGING("일반숙박시설", false),
    RESTAURANT("일반음식점", false),
    NATURE_PARK("자연공원", false),
    NATURE_TRAINING_FACILITY("자연권수련시설", false),
    FUNERAL_HALL("장례식장", false),
    DISABILITY_WELFARE_FACILITY("장애인복지시설", false),
    JUNIOR_COLLEGE("전문대학", false),
    EXHIBITION_HALL("전시장", false),
    TELEPHONE_EXCHANGE("전신전화국", false),
    RELIGIOUS_ASSEMBLY_HALL("종교집회장", false),
    GENERAL_HOSPITAL("종합병원", false),
    PARKING_LOT("주차장", false),
    MIDDLE_SCHOOL("중학교", false),
    COMMUNITY_CHILD_CENTER("지역아동센터", false),
    COMMUNITY_CENTER("지역자치센터", false),
    ASSEMBLY_HALL("집회장", false),
    GYMNASIUM("체육관", false),
    ELEMENTARY_SCHOOL("초등학교", false),
    SPECIAL_SCHOOL("특수학교", false),
    POLICE_SUBSTATION("파출소, 지구대", false),
    KOREA_DISABLED_EMPLOYMENT_AGENCY("한국장애인고용공단 및 지사", false),
    CREMATION_FACILITY("화장시설", false),
    REST_AREA("휴게소", false),
    CAFE_BAKERY("휴게음식점·제과점", false),
    CAFE_BAKERY_ETC("휴게음식점·제과점 등", false);

    private final String label;
    private final boolean isActive;

    FacilityType(String label, boolean isActive) {
        this.label = label;
        this.isActive = isActive;
    }

    public String getLabel() {
        return label;
    }

    public boolean isActive() {
        return isActive;
    }
}
