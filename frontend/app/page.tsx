"use client"
import React, { useState, useEffect, useRef } from 'react';

export default function App() {
    const [map, setMap] = useState(null);
    const [places, setPlaces] = useState([]);
    const [selectedCategory, setSelectedCategory] = useState('카페');
    const [currentPosition, setCurrentPosition] = useState(null);
    const [apiKey, setApiKey] = useState('');
    const [isApiKeySet, setIsApiKeySet] = useState(false);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [errorMsg, setErrorMsg] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [selectedPlace, setSelectedPlace] = useState(null);
    const [routeInfo, setRouteInfo] = useState(null);
    const mapContainer = useRef(null);
    const markersRef = useRef([]);
    const polylineRef = useRef(null);
    const routeMarkersRef = useRef([]);

    const categories = [
        { name: '카페', emoji: '☕', keyword: '카페' },
        { name: '편의점', emoji: '🏪', keyword: '편의점' },
        { name: '음식점', emoji: '🍽️', keyword: '음식점' },
        { name: '마트', emoji: '🛒', keyword: '마트' }
    ];

    useEffect(() => {
        const fetchApiKey = async () => {
            try {
                const res = await fetch('/api/v1/locations/kakao/key');

                const contentType = res.headers.get('content-type') || '';
                let key = '';

                if (contentType.includes('application/json')) {
                    const data = await res.json();
                    // 백엔드가 { key: 'JS_KEY_VALUE' } 또는 "JS_KEY_VALUE" 둘 다 가능
                    key = typeof data === 'string' ? data : (data?.key ?? '');
                } else {
                    // plain text 반환 처리
                    key = await res.text();
                }

                key = (key || '').trim();
                if (key) {
                    setApiKey(key);
                    setIsApiKeySet(true);
                } else {
                    console.warn('Empty API key from backend');
                }
            } catch (err) {
                console.error('Failed to fetch Kakao key:', err);
            }
        };

        fetchApiKey();
    }, []);

    useEffect(() => {
        if (!isApiKeySet || !apiKey) return;

        setIsLoading(true);
        setErrorMsg('');

        const script = document.createElement('script');
        script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${apiKey}&autoload=false&libraries=services`;
        script.async = true;

        script.onload = () => {
            window.kakao.maps.load(() => {
                initMap();
            });
        };

        script.onerror = () => {
            setErrorMsg('Kakao Map API 로드 실패. API 키를 확인해주세요.');
            setIsLoading(false);
        };

        document.head.appendChild(script);

        return () => {
            if (script.parentNode) {
                script.parentNode.removeChild(script);
            }
        };
    }, [isApiKeySet, apiKey]);

    const initMap = () => {
        if (!mapContainer.current || !window.kakao) {
            setErrorMsg('지도를 초기화할 수 없습니다.');
            setIsLoading(false);
            return;
        }

        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const lat = position.coords.latitude;
                    const lng = position.coords.longitude;

                    const mapOption = {
                        center: new window.kakao.maps.LatLng(lat, lng),
                        level: 3
                    };

                    const newMap = new window.kakao.maps.Map(mapContainer.current, mapOption);
                    setMap(newMap);
                    setCurrentPosition({ lat, lng });
                    setIsLoading(false);
                    setErrorMsg('');

                    const markerPosition = new window.kakao.maps.LatLng(lat, lng);

                    // 현재 위치 마커 (파란색)
                    const imageSrc = 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_red.png';
                    const imageSize = new window.kakao.maps.Size(44, 49);
                    const markerImage = new window.kakao.maps.MarkerImage(imageSrc, imageSize);

                    const marker = new window.kakao.maps.Marker({
                        position: markerPosition,
                        map: newMap,
                        image: markerImage,
                        zIndex: 3
                    });

                    const circle = new window.kakao.maps.Circle({
                        center: markerPosition,
                        radius: 50,
                        strokeWeight: 2,
                        strokeColor: '#ef4444',
                        strokeOpacity: 0.8,
                        fillColor: '#ef4444',
                        fillOpacity: 0.3
                    });
                    circle.setMap(newMap);
                },
                (error) => {
                    setErrorMsg('위치 권한을 허용해주세요. 브라우저 설정에서 위치 권한을 확인하세요.');
                    setIsLoading(false);

                    const defaultLat = 37.5665;
                    const defaultLng = 126.9780;

                    const mapOption = {
                        center: new window.kakao.maps.LatLng(defaultLat, defaultLng),
                        level: 5
                    };

                    const newMap = new window.kakao.maps.Map(mapContainer.current, mapOption);
                    setMap(newMap);
                    setCurrentPosition({ lat: defaultLat, lng: defaultLng });
                }
            );
        }
    };

    // 도보 경로 찾기 함수
    const findWalkingRoute = async (destination) => {
        if (!currentPosition || !destination) return;

        // 기존 경로 지우기
        clearRoute();

        try {
            // Kakao REST API를 사용하여 도보 경로 찾기
            const response = await fetch(
                `https://apis-navi.kakaomobility.com/v1/directions?origin=${currentPosition.lng},${currentPosition.lat}&destination=${destination.x},${destination.y}&priority=RECOMMEND&car_fuel=GASOLINE&car_hipass=false&alternatives=false&road_details=false`,
                {
                    headers: {
                        'Authorization': `KakaoAK e57fa4c3002e088c8d00b3dd4ab47fae`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            if (!response.ok) {
                // REST API 키가 필요한 경우, 직선 경로로 표시
                drawStraightLine(destination);
                return;
            }

            const data = await response.json();

            if (data.routes && data.routes.length > 0) {
                const route = data.routes[0];
                drawRoute(route, destination);
            } else {
                drawStraightLine(destination);
            }
        } catch (error) {
            // API 호출 실패 시 직선 경로로 표시
            drawStraightLine(destination);
        }
    };

    // 직선 경로 그리기 (간단한 버전)
    const drawStraightLine = (destination) => {
        const startPos = new window.kakao.maps.LatLng(currentPosition.lat, currentPosition.lng);
        const endPos = new window.kakao.maps.LatLng(destination.y, destination.x);

        const linePath = [startPos, endPos];

        const polyline = new window.kakao.maps.Polyline({
            path: linePath,
            strokeWeight: 5,
            strokeColor: '#3b82f6',
            strokeOpacity: 0.7,
            strokeStyle: 'solid'
        });

        polyline.setMap(map);
        polylineRef.current = polyline;

        // 도착지 마커
        const marker = new window.kakao.maps.Marker({
            position: endPos,
            map: map
        });
        routeMarkersRef.current.push(marker);

        // 거리 계산
        const distance = getDistance(currentPosition.lat, currentPosition.lng, destination.y, destination.x);
        const walkingTime = Math.ceil(distance / 67); // 도보 속도 약 4km/h = 67m/min

        setRouteInfo({
            distance: distance,
            duration: walkingTime,
            type: '직선 거리'
        });

        // 지도 범위 조정
        const bounds = new window.kakao.maps.LatLngBounds();
        bounds.extend(startPos);
        bounds.extend(endPos);
        map.setBounds(bounds);
    };

    // 경로 그리기
    const drawRoute = (route, destination) => {
        const path = [];

        route.sections.forEach(section => {
            section.roads.forEach(road => {
                road.vertexes.forEach((vertex, index) => {
                    if (index % 2 === 0) {
                        path.push(new window.kakao.maps.LatLng(
                            road.vertexes[index + 1],
                            road.vertexes[index]
                        ));
                    }
                });
            });
        });

        const polyline = new window.kakao.maps.Polyline({
            path: path,
            strokeWeight: 5,
            strokeColor: '#3b82f6',
            strokeOpacity: 0.7,
            strokeStyle: 'solid'
        });

        polyline.setMap(map);
        polylineRef.current = polyline;

        // 도착지 마커
        const endPos = new window.kakao.maps.LatLng(destination.y, destination.x);
        const marker = new window.kakao.maps.Marker({
            position: endPos,
            map: map
        });
        routeMarkersRef.current.push(marker);

        setRouteInfo({
            distance: route.summary.distance,
            duration: Math.ceil(route.summary.duration / 60),
            type: '도보 경로'
        });

        // 지도 범위 조정
        const bounds = new window.kakao.maps.LatLngBounds();
        path.forEach(point => bounds.extend(point));
        map.setBounds(bounds);
    };

    // 거리 계산 (Haversine formula)
    const getDistance = (lat1, lon1, lat2, lon2) => {
        const R = 6371e3; // 지구 반지름 (미터)
        const φ1 = lat1 * Math.PI / 180;
        const φ2 = lat2 * Math.PI / 180;
        const Δφ = (lat2 - lat1) * Math.PI / 180;
        const Δλ = (lon2 - lon1) * Math.PI / 180;

        const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
            Math.cos(φ1) * Math.cos(φ2) *
            Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(R * c);
    };

    // 경로 지우기
    const clearRoute = () => {
        if (polylineRef.current) {
            polylineRef.current.setMap(null);
            polylineRef.current = null;
        }
        routeMarkersRef.current.forEach(marker => marker.setMap(null));
        routeMarkersRef.current = [];
        setRouteInfo(null);
        setSelectedPlace(null);
    };

    const searchPlaces = (keyword) => {
        if (!map || !window.kakao || !currentPosition) return;

        markersRef.current.forEach(marker => marker.setMap(null));
        markersRef.current = [];
        clearRoute();

        const ps = new window.kakao.maps.services.Places();
        const searchOption = {
            location: new window.kakao.maps.LatLng(currentPosition.lat, currentPosition.lng),
            radius: 2000
        };

        ps.keywordSearch(keyword, (data, status) => {
            if (status === window.kakao.maps.services.Status.OK) {
                setPlaces(data);

                data.forEach((place, index) => {
                    const markerPosition = new window.kakao.maps.LatLng(place.y, place.x);

                    const marker = new window.kakao.maps.Marker({
                        position: markerPosition,
                        map: map
                    });

                    const infowindow = new window.kakao.maps.InfoWindow({
                        content: `<div style="padding:5px;font-size:12px;white-space:nowrap;">${index + 1}. ${place.place_name}</div>`
                    });

                    window.kakao.maps.event.addListener(marker, 'mouseover', () => {
                        infowindow.open(map, marker);
                    });

                    window.kakao.maps.event.addListener(marker, 'mouseout', () => {
                        infowindow.close();
                    });

                    window.kakao.maps.event.addListener(marker, 'click', () => {
                        map.setCenter(markerPosition);
                        map.setLevel(2);
                    });

                    markersRef.current.push(marker);
                });

                if (data.length > 0) {
                    const bounds = new window.kakao.maps.LatLngBounds();
                    data.forEach(place => {
                        bounds.extend(new window.kakao.maps.LatLng(place.y, place.x));
                    });
                    map.setBounds(bounds);
                }
            } else if (status === window.kakao.maps.services.Status.ZERO_RESULT) {
                setPlaces([]);
                alert('검색 결과가 없습니다.');
            } else {
                setPlaces([]);
                alert('검색 중 오류가 발생했습니다.');
            }
        }, searchOption);
    };

    const handleCategoryClick = (category) => {
        setSelectedCategory(category.name);
        setSearchKeyword(category.keyword);
        searchPlaces(category.keyword);
    };

    const handleSearch = () => {
        if (searchKeyword.trim()) {
            searchPlaces(searchKeyword);
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter') {
            handleSearch();
        }
    };

    const handleApiKeySubmit = () => {
        if (apiKey.trim()) {
            setIsApiKeySet(true);
        } else {
            alert('API 키를 입력해주세요.');
        }
    };

    // 장소 클릭 시 경로 표시
    const handlePlaceClick = (place) => {
        setSelectedPlace(place);
        const position = new window.kakao.maps.LatLng(place.y, place.x);
        map.setCenter(position);
        map.setLevel(3);
        findWalkingRoute(place);
    };

    if (!isApiKeySet) {
        return (
            <div style={{
                minHeight: '100vh',
                background: 'linear-gradient(to bottom right, #dbeafe, #e0e7ff)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                padding: '20px'
            }}>
                <div style={{
                    background: 'white',
                    borderRadius: '16px',
                    boxShadow: '0 20px 50px rgba(0,0,0,0.1)',
                    padding: '40px',
                    maxWidth: '500px',
                    width: '100%'
                }}>
                    <div style={{ textAlign: 'center', marginBottom: '30px' }}>
                        <div style={{ fontSize: '64px', marginBottom: '16px' }}>📍</div>
                        <h1 style={{ fontSize: '32px', fontWeight: 'bold', color: '#1f2937', marginBottom: '8px' }}>
                            내 주변 찾기
                        </h1>
                        <p style={{ color: '#6b7280' }}>도보 경로 안내 포함</p>
                    </div>

                    <div style={{ marginBottom: '16px' }}>
                        <label style={{ display: 'block', fontSize: '14px', fontWeight: '500', color: '#374151', marginBottom: '8px' }}>
                            Kakao API Key (JavaScript 키)
                        </label>
                        <input
                            type="text"
                            value={apiKey}
                            onChange={(e) => setApiKey(e.target.value)}
                            onKeyPress={(e) => e.key === 'Enter' && handleApiKeySubmit()}
                            placeholder="예: 1234567890abcdef1234567890abcdef"
                            style={{
                                width: '100%',
                                padding: '12px 16px',
                                border: '2px solid #d1d5db',
                                borderRadius: '8px',
                                fontSize: '16px',
                                boxSizing: 'border-box'
                            }}
                        />
                    </div>

                    <button
                        onClick={handleApiKeySubmit}
                        style={{
                            width: '100%',
                            background: '#2563eb',
                            color: 'white',
                            padding: '12px',
                            borderRadius: '8px',
                            fontWeight: '600',
                            fontSize: '16px',
                            border: 'none',
                            cursor: 'pointer',
                            transition: 'background 0.2s'
                        }}
                        onMouseEnter={(e) => e.target.style.background = '#1d4ed8'}
                        onMouseLeave={(e) => e.target.style.background = '#2563eb'}
                    >
                        시작하기
                    </button>

                    <div style={{
                        marginTop: '24px',
                        padding: '16px',
                        background: '#fef3c7',
                        borderRadius: '8px',
                        border: '1px solid #fbbf24'
                    }}>
                        <p style={{ fontSize: '14px', color: '#92400e', lineHeight: '1.6', margin: 0 }}>
                            <strong>⚠️ 중요:</strong> 반드시 <strong>JavaScript 키</strong>를 입력하세요!
                        </p>
                    </div>

                    <div style={{
                        marginTop: '16px',
                        padding: '16px',
                        background: '#eff6ff',
                        borderRadius: '8px'
                    }}>
                        <p style={{ fontSize: '14px', color: '#1f2937', lineHeight: '1.8', margin: 0 }}>
                            <strong>📝 API 키 발급:</strong><br />
                            <a href="https://developers.kakao.com" target="_blank" rel="noopener noreferrer" style={{ color: '#2563eb', textDecoration: 'underline' }}>Kakao Developers</a> → 내 애플리케이션 → 앱 키 → JavaScript 키 복사
                        </p>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', background: '#f9fafb' }}>
            <div style={{ background: 'white', boxShadow: '0 2px 4px rgba(0,0,0,0.1)', padding: '16px' }}>
                <div style={{ maxWidth: '1280px', margin: '0 auto' }}>
                    <h1 style={{ fontSize: '24px', fontWeight: 'bold', color: '#1f2937', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <span>📍</span> 내 주변 찾기 + 도보 경로
                    </h1>

                    {errorMsg && (
                        <div style={{
                            background: '#fee2e2',
                            border: '1px solid #fca5a5',
                            borderRadius: '8px',
                            padding: '12px',
                            marginBottom: '16px',
                            color: '#991b1b'
                        }}>
                            ⚠️ {errorMsg}
                        </div>
                    )}

                    {routeInfo && (
                        <div style={{
                            background: '#dbeafe',
                            border: '1px solid #3b82f6',
                            borderRadius: '8px',
                            padding: '12px',
                            marginBottom: '16px',
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center'
                        }}>
                            <div style={{ color: '#1e40af' }}>
                                🚶‍♂️ <strong>{routeInfo.type}</strong>: 약 {routeInfo.distance}m (도보 {routeInfo.duration}분)
                            </div>
                            <button
                                onClick={clearRoute}
                                style={{
                                    background: '#ef4444',
                                    color: 'white',
                                    padding: '6px 12px',
                                    borderRadius: '6px',
                                    border: 'none',
                                    cursor: 'pointer',
                                    fontSize: '14px'
                                }}
                            >
                                경로 지우기
                            </button>
                        </div>
                    )}

                    <div style={{ display: 'flex', gap: '8px', marginBottom: '16px' }}>
                        <input
                            type="text"
                            value={searchKeyword}
                            onChange={(e) => setSearchKeyword(e.target.value)}
                            onKeyPress={handleKeyPress}
                            placeholder="검색어를 입력하세요 (예: 스타벅스, 이디야)"
                            style={{
                                flex: 1,
                                padding: '8px 16px',
                                border: '1px solid #d1d5db',
                                borderRadius: '8px',
                                fontSize: '16px'
                            }}
                        />
                        <button
                            onClick={handleSearch}
                            disabled={!map}
                            style={{
                                background: map ? '#2563eb' : '#9ca3af',
                                color: 'white',
                                padding: '8px 24px',
                                borderRadius: '8px',
                                fontWeight: '600',
                                border: 'none',
                                cursor: map ? 'pointer' : 'not-allowed',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '8px'
                            }}
                        >
                            🔍 검색
                        </button>
                    </div>

                    <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                        {categories.map((category) => (
                            <button
                                key={category.name}
                                onClick={() => handleCategoryClick(category)}
                                disabled={!map}
                                style={{
                                    padding: '8px 16px',
                                    borderRadius: '8px',
                                    fontWeight: '500',
                                    border: 'none',
                                    cursor: map ? 'pointer' : 'not-allowed',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '8px',
                                    background: selectedCategory === category.name ? '#2563eb' : '#f3f4f6',
                                    color: selectedCategory === category.name ? 'white' : '#374151',
                                    opacity: map ? 1 : 0.5
                                }}
                            >
                                <span>{category.emoji}</span>
                                {category.name}
                            </button>
                        ))}
                    </div>
                </div>
            </div>

            <div style={{ flex: 1, display: 'flex', overflow: 'hidden' }}>
                <div style={{ flex: 1, position: 'relative' }}>
                    <div ref={mapContainer} style={{ width: '100%', height: '100%', background: '#e5e7eb' }} />
                    {!map && !isLoading && (
                        <div style={{
                            position: 'absolute',
                            top: '50%',
                            left: '50%',
                            transform: 'translate(-50%, -50%)',
                            textAlign: 'center',
                            color: '#6b7280'
                        }}>
                            <div style={{ fontSize: '48px', marginBottom: '16px' }}>🗺️</div>
                            <p>지도를 불러오는 중입니다...</p>
                        </div>
                    )}
                </div>

                <div style={{ width: '384px', background: 'white', boxShadow: '-2px 0 8px rgba(0,0,0,0.1)', overflowY: 'auto' }}>
                    <div style={{ padding: '16px' }}>
                        <h2 style={{ fontSize: '18px', fontWeight: 'bold', color: '#1f2937', marginBottom: '8px' }}>
                            검색 결과 ({places.length}개)
                        </h2>
                        <p style={{ fontSize: '12px', color: '#6b7280', marginBottom: '16px' }}>
                            💡 장소를 클릭하면 도보 경로가 표시됩니다
                        </p>

                        {places.length === 0 ? (
                            <div style={{ textAlign: 'center', padding: '48px 0', color: '#6b7280' }}>
                                <div style={{ fontSize: '48px', marginBottom: '8px', opacity: 0.5 }}>🔍</div>
                                <p>검색 결과가 없습니다</p>
                                <p style={{ fontSize: '14px', marginTop: '4px' }}>카테고리를 선택하거나 검색해보세요</p>
                            </div>
                        ) : (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                                {places.map((place, index) => (
                                    <div
                                        key={place.id}
                                        onClick={() => handlePlaceClick(place)}
                                        style={{
                                            padding: '16px',
                                            border: selectedPlace?.id === place.id ? '2px solid #3b82f6' : '1px solid #e5e7eb',
                                            borderRadius: '8px',
                                            cursor: 'pointer',
                                            transition: 'all 0.2s',
                                            background: selectedPlace?.id === place.id ? '#eff6ff' : 'white'
                                        }}
                                        onMouseEnter={(e) => {
                                            if (selectedPlace?.id !== place.id) {
                                                e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
                                                e.currentTarget.style.borderColor = '#3b82f6';
                                            }
                                        }}
                                        onMouseLeave={(e) => {
                                            if (selectedPlace?.id !== place.id) {
                                                e.currentTarget.style.boxShadow = 'none';
                                                e.currentTarget.style.borderColor = '#e5e7eb';
                                            }
                                        }}
                                    >
                                        <div style={{ display: 'flex', gap: '12px' }}>
                                            <div style={{
                                                background: selectedPlace?.id === place.id ? '#3b82f6' : '#2563eb',
                                                color: 'white',
                                                width: '24px',
                                                height: '24px',
                                                borderRadius: '50%',
                                                display: 'flex',
                                                alignItems: 'center',
                                                justifyContent: 'center',
                                                fontSize: '14px',
                                                fontWeight: 'bold',
                                                flexShrink: 0
                                            }}>
                                                {index + 1}
                                            </div>
                                            <div style={{ flex: 1, minWidth: 0 }}>
                                                <h3 style={{
                                                    fontWeight: '600',
                                                    color: '#1f2937',
                                                    marginBottom: '4px',
                                                    overflow: 'hidden',
                                                    textOverflow: 'ellipsis',
                                                    whiteSpace: 'nowrap'
                                                }}>
                                                    {place.place_name}
                                                    {selectedPlace?.id === place.id && <span style={{ marginLeft: '8px' }}>🚶‍♂️</span>}
                                                </h3>
                                                <p style={{ fontSize: '14px', color: '#6b7280', marginBottom: '4px' }}>
                                                    {place.category_name}
                                                </p>
                                                <p style={{ fontSize: '14px', color: '#9ca3af' }}>
                                                    {place.road_address_name || place.address_name}
                                                </p>
                                                {place.phone && (
                                                    <p style={{ fontSize: '14px', color: '#2563eb', marginTop: '4px' }}>
                                                        📞 {place.phone}
                                                    </p>
                                                )}
                                                {place.distance && (
                                                    <p style={{ fontSize: '12px', color: '#d1d5db', marginTop: '4px' }}>
                                                        📍 {place.distance}m
                                                    </p>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}