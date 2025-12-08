"use client";

import React, { useEffect, useState } from "react";
import { useSearchParams, useRouter } from "next/navigation";
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
        const linePaths: kakao.maps.LatLng[] = [];

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
          <div className={styles.loading}>
            서버 오류로 인해 길찾기 기능을 요청할 수 없습니다.
          </div>
        </div>
    );
  }

  return (
            <div style={{ position: "relative", width: "100%", height: "100vh" }}>
              <div className={styles.topBar}>
                <button onClick={() => router.push('/')} className={styles.backButton}>
                  뒤로 가기
                </button>
              </div>
              <div id="map" style={{ width: "100%", height: "100%" }} />

        {pathData && "pathSummary" in pathData.data && (
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
            </div>
        )}

        {pathData && "uri" in pathData.data && (
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
                ⚠️ 해당 구간은 서비스 제공 구역이 아니거나 너무 멉니다.<br />
                카카오맵 외부 경로로 안내됩니다.
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