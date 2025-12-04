"use client";

import React, { createContext, useState, useEffect, useContext } from 'react';

// 위치 정보 타입 정의
interface ILocation {
    lat: number;
    lng: number;
    timestamp: number;
}

// 서울 시청 좌표를 기본값으로 사용 (위도 37.5665, 경도 126.9780)
const defaultLocation: ILocation = { lat: 37.5665, lng: 126.9780, timestamp: Date.now() };

// Context 생성 (기본값으로 defaultLocation 사용)
const LocationContext = createContext<ILocation>(defaultLocation);

export const LocationProvider: React.FC<React.PropsWithChildren<{}>> = ({ children }) => {
    const [location, setLocation] = useState<ILocation>(defaultLocation);

    useEffect(() => {
        if (!navigator.geolocation) {
            console.error("Geolocation is not supported by this browser.");
            return;
        }
        // 위치 변경 시마다 호출되는 성공 콜백 함수
        const onSuccess: PositionCallback = (position) => {
            const { latitude, longitude } = position.coords;
            const timestamp = position.timestamp || Date.now();
            setLocation({ lat: latitude, lng: longitude, timestamp });
        };
        // 오류 발생 시 호출되는 콜백 함수 (권한 거부, 위치 불능 등)
        const onError: PositionErrorCallback = (error) => {
            console.error("Error watching position:", error);
            // 오류 시 기본 위치로 설정 (timestamp는 현재 시각)
            setLocation({ ...defaultLocation, timestamp: Date.now() });
        };
        // 위치 추적 시작 (지속적인 watch)
        const watchId: number = navigator.geolocation.watchPosition(onSuccess, onError);
        // 언마운트 시 추적 중지
        return () => {
            navigator.geolocation.clearWatch(watchId);
        };
    }, []);  // 빈 배열 의존성으로 처음 마운트시에만 실행

    return (
        <LocationContext.Provider value={location}>
            {children}
        </LocationContext.Provider>
    );
};

// 커스텀 훅: 위치 정보 사용
export const useLocation = (): ILocation => {
    return useContext(LocationContext);
};
