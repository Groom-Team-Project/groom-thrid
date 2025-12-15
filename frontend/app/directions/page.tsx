"use client";

export const dynamic = 'force-dynamic'

import React, { useEffect, useState, useRef } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { updateLocation } from "@/lib/location";
import styles from "./page.module.css";

interface BaseNode {
  type: "Point" | "LineString";
  index: number;
  name: string;
  description: string;
  distance: number | null;
  roadType: number | null;
  time: number | null;
  categoryRoadType: number | null;
  facilityType: number | null;
  coordinates: [number, number][];
}

export interface PointNode extends BaseNode {
  type: "Point";
}

export interface LineStringNode extends BaseNode {
  type: "LineString";
}

export type PathNode = PointNode | LineStringNode;

interface PathSummary {
  totalDistance: number;
  totalTime: number;
}

interface InternalPathResponse {
  status: string;
  code: number;
  message: string;
  data: {
    pathNodeList: PathNode[];
    pathSummary: PathSummary;
  };
}

interface FallbackPathResponse {
  status: string;
  code: number;
  message: string;
  data: {
    uri: string;
  };
}

type PathData = InternalPathResponse | FallbackPathResponse;

interface SimpleCoord {
  lat: number;
  lng: number;
}

const DirectionsPage: React.FC = () => {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [pathData, setPathData] = useState<PathData | null>(null);
  const [startCoord, setStartCoord] = useState<SimpleCoord | null>(null);
  const [endCoord, setEndCoord] = useState<SimpleCoord | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const [isNavigating, setIsNavigating] = useState(false);

  useEffect(() => {
    const startLatStr = searchParams.get("start-lat");
    const startLngStr = searchParams.get("start-lng");
    const endLatStr = searchParams.get("end-lat");
    const endLngStr = searchParams.get("end-lng");
    const startName = searchParams.get("start-name");
    const endName = searchParams.get("end-name");

    if (!startLatStr || !startLngStr || !endLatStr || !endLngStr || !startName || !endName) return;

    const startLat = Number(startLatStr);
    const startLng = Number(startLngStr);
    const endLat = Number(endLatStr);
    const endLng = Number(endLngStr);

    setStartCoord({ lat: startLat, lng: startLng });
    setEndCoord({ lat: endLat, lng: endLng });

    const requestBody = {
      startY: startLat,
      startX: startLng,
      endY: endLat,
      endX: endLng,
      startName,
      endName,
    };

    const controller = new AbortController();
    const timeoutId = setTimeout(() => {
      controller.abort();
      setIsLoading(false);
      setHasError(true);
    }, 30000);

    const fetchPath = async () => {
      try {
        const res = await fetch("/api/v1/paths", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(requestBody),
          signal: controller.signal,
        });

        const data = await res.json();
        setPathData(data);
        setIsLoading(false);
      } catch (error) {
        console.error("경로 데이터를 불러오는 중 오류:", error);
        setHasError(true);
        setIsLoading(false);
      } finally {
        clearTimeout(timeoutId);
      }
    };

    fetchPath();
  }, [searchParams]);

  useEffect(() => {
    if (!pathData || !startCoord || !endCoord) return;

    const mapContainer = document.getElementById("map");
    if (!mapContainer) return;

    const drawMap = () => {
      const kakaoMaps = (window as any).kakao.maps;

      const map = new kakaoMaps.Map(mapContainer, {
        center: new kakaoMaps.LatLng(startCoord.lat, startCoord.lng),
        level: 5,
      });

      new kakaoMaps.Marker({ position: new kakaoMaps.LatLng(startCoord.lat, startCoord.lng), map });
      new kakaoMaps.Marker({ position: new kakaoMaps.LatLng(endCoord.lat, endCoord.lng), map });

      if ("pathNodeList" in pathData.data) {
        const nodes = pathData.data.pathNodeList;
        const linePaths: any[] = [];

        nodes.forEach((node) => {
          if (node.type === "LineString") {
            node.coordinates.forEach(([lng, lat]) => {
              linePaths.push(new kakaoMaps.LatLng(lat, lng));
            });
          }
        });

        if (linePaths.length > 1) {
          new kakaoMaps.Polyline({
            path: linePaths,
            strokeWeight: 5,
            strokeColor: "#005AFF",
            strokeOpacity: 0.8,
            strokeStyle: "solid",
            map,
          });

          const bounds = new kakaoMaps.LatLngBounds();
          linePaths.forEach((p) => bounds.extend(p));
          map.setBounds(bounds);
        }
      } else {
        const bounds = new kakaoMaps.LatLngBounds();
        bounds.extend(new kakaoMaps.LatLng(startCoord.lat, startCoord.lng));
        bounds.extend(new kakaoMaps.LatLng(endCoord.lat, endCoord.lng));
        map.setBounds(bounds);
      }
    };

    if (!(window as any).kakao || !(window as any).kakao.maps) {
      const script = document.createElement("script");
      const kakaoApiKey = process.env.NEXT_PUBLIC_KAKAO_MAP_KEY;
      script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoApiKey || ""}&autoload=false`;
      script.onload = () => (window as any).kakao.maps.load(drawMap);
      document.head.appendChild(script);
    } else {
      (window as any).kakao.maps.load(drawMap);
    }
  }, [pathData, startCoord, endCoord]);

  // 사용자 위치 추적 및 백엔드 업데이트 (주기적 전송)
  useEffect(() => {
    if (!isNavigating || !endCoord) return;

    console.log("[위치 추적] 🎯 길안내 시작 - 실시간 위치 추적 활성화");
    let updateCount = 0;
    let isActive = true;

    // 주기적으로 위치 가져와서 전송 (3초마다)
    const locationInterval = setInterval(async () => {
      if (!isActive) return;

      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const currentLat = position.coords.latitude;
          const currentLng = position.coords.longitude;
          updateCount++;

          console.log(`[위치 추적] 📍 위치 업데이트 #${updateCount}:`, currentLat, currentLng);

          // 백엔드에 위치 업데이트 전송
          try {
            await updateLocation(currentLat, currentLng);
            console.log(`[위치 추적] ✅ 백엔드 전송 성공 #${updateCount}`);
          } catch (error) {
            console.error(`[위치 추적] ❌ 백엔드 전송 실패 #${updateCount}:`, error);
          }

          // 목적지 도착 확인
          const latDiff = Math.abs(currentLat - endCoord.lat);
          const lngDiff = Math.abs(currentLng - endCoord.lng);

          const threshold = 0.001; // 약 100m

          if (latDiff < threshold && lngDiff < threshold) {
            console.log("[위치 추적] 🏁 목표 지점에 도착! 길안내 종료");

            fetch("/api/v1/paths/navigation", {
              method: "DELETE",
              headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
              },
            }).then((res) => {
              if (res.ok) {
                setIsNavigating(false);
                console.log("[위치 추적] 🔌 길안내 종료 완료");
              }
            });
          }
        },
        (error) => {
          console.error(`[위치 추적] ❌ 위치 조회 실패 #${updateCount + 1}:`, error);
          console.error("[위치 추적] 오류 코드:", error.code);
          console.error("[위치 추적] 오류 메시지:", error.message);
        },
        {
          enableHighAccuracy: true,
          maximumAge: 0,
          timeout: 10000
        }
      );
    }, 3000); // 3초마다 위치 전송

    return () => {
      console.log("[위치 추적] 🔌 위치 추적 해제");
      isActive = false;
      clearInterval(locationInterval);
    };
  }, [isNavigating, endCoord]);

  const toggleNavigation = async () => {
    if (!startCoord || !endCoord) return;
    const startName = searchParams.get("start-name") || "출발지";
    const endName = searchParams.get("end-name") || "도착지";
    const body = {
      startX: startCoord.lng.toString(),
      startY: startCoord.lat.toString(),
      endX: endCoord.lng.toString(),
      endY: endCoord.lat.toString(),
      startName,
      endName,
    };
    try {
      const res = await fetch("/api/v1/paths/navigation", {
        method: isNavigating ? "DELETE" : "POST",
        headers: { "Content-Type": "application/json",
                     Authorization: `Bearer ${localStorage.getItem('accessToken')}`, },
        body: isNavigating ? undefined : JSON.stringify(body),
      });
      if (res.ok) setIsNavigating(!isNavigating);
    } catch (e) {
      console.error("길안내 상태 전환 실패", e);
    }
  };

  let distanceStr = "";
  let timeStr = "";

  if (pathData && "pathSummary" in pathData.data) {
    const { totalDistance, totalTime } = pathData.data.pathSummary;
    distanceStr = totalDistance >= 1000 ? `${(totalDistance / 1000).toFixed(1)} km` : `${totalDistance} m`;
    const hours = Math.floor(totalTime / 3600);
    const minutes = Math.floor((totalTime % 3600) / 60);
    timeStr = hours > 0 ? `${hours}시간 ${minutes}분` : minutes > 0 ? `${minutes}분` : `${totalTime}초`;
  }

  if (isLoading) {
    return (
        <div className={styles.loading}>
          <div className={styles.spinner}></div>
        </div>
    );
  }

  if (hasError) {
    return (
        <div className={styles.container}>
          <div className={styles.topBar}>
            <button onClick={() => router.push("/")} className={styles.backButton}>
              ←
            </button>
          </div>
          <div className={styles.loading}>서버 오류로 인해 길찾기 기능을 요청할 수 없습니다.</div>
        </div>
    );
  }

  return (
      <div style={{ position: "relative", width: "100%", height: "100vh" }}>
        <div className={styles.topBar}>
          <button onClick={() => router.push("/")} className={styles.backButton}>
            뒤로 가기
          </button>
        </div>
        <div id="map" style={{ width: "100%", height: "100%" }} />

        {(pathData && "pathSummary" in pathData.data) && (
            <div style={{
              position: "absolute",
              bottom: "10px",
              left: "0",
              width: "100%",
              padding: "12px",
              backgroundColor: "#FFFFFF",
              zIndex: 10,
              boxSizing: "border-box",
              borderTop: "1px solid #ccc",
            }}>
              <p>
                총 거리: <strong>{distanceStr}</strong><br />
                예상 소요 시간: <strong>{timeStr}</strong>
              </p>
              <button
                  onClick={toggleNavigation}
                  style={{
                    position: "absolute",
                    right: "12px",
                    top: "12px",
                    padding: "8px 12px",
                    backgroundColor: isNavigating ? "#FF5555" : "#007AFF",
                    color: "#fff",
                    border: "none",
                    borderRadius: "4px",
                    cursor: "pointer",
                  }}>
                {isNavigating ? "길안내 종료" : "길안내 시작"}
              </button>
            </div>
        )}

        {(pathData && "uri" in pathData.data) && (
            <div style={{
              position: "absolute",
              bottom: "10px",
              left: "0",
              width: "100%",
              padding: "12px",
              backgroundColor: "#FEEDBE",
              zIndex: 10,
              boxSizing: "border-box",
              borderTop: "1px solid #ccc",
            }}>
              <p style={{ marginBottom: "8px" }}>
                ⚠️ 다음 오류로 인해 기능을 사용할 수 없습니다. 카카오맵을 이용해주십시오.<br />
                {pathData.message}
              </p>
              <a href={pathData.data.uri} target="_blank" rel="noopener noreferrer" style={{ color: "#005AFF", textDecoration: "underline" }}>
                카카오맵에서 경로 보기
              </a>
            </div>
        )}
      </div>
  );
};

export default DirectionsPage;
