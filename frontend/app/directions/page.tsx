"use client";

import React, { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";

/* -----------------------------
   PathNode 타입 정의 (수정된 버전)
----------------------------- */

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
  coordinates: [number, number][]; // 모든 타입에 존재
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

/* 좌표 단순 lat/lng 객체 */
interface SimpleCoord {
  lat: number;
  lng: number;
}

/* ----------------------------------------------------
   여기까지 타입 수정 완료
----------------------------------------------------- */

const DirectionsPage: React.FC = () => {
  const searchParams = useSearchParams();
  const [pathData, setPathData] = useState<PathData | null>(null);
  const [startCoord, setStartCoord] = useState<SimpleCoord | null>(null);
  const [endCoord, setEndCoord] = useState<SimpleCoord | null>(null);

  /* ------------------------------
     API 요청 및 좌표 초기화
  ------------------------------ */
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

    const fetchPath = async () => {
      try {
        const res = await fetch("/api/v1/paths", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(requestBody),
        });

        const data = await res.json();
        setPathData(data);
      } catch (error) {
        console.error("경로 데이터를 불러오는 중 오류:", error);
      }
    };

    fetchPath();
  }, [searchParams]);

  /* ------------------------------
     지도 렌더링 처리
  ------------------------------ */
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

      /* 시작/끝 마커 */
      new kakaoMaps.Marker({ position: new kakaoMaps.LatLng(startCoord.lat, startCoord.lng), map });
      new kakaoMaps.Marker({ position: new kakaoMaps.LatLng(endCoord.lat, endCoord.lng), map });

      /* ------------------------------
         내부 경로 타입일 경우: Polyline 생성
      ------------------------------ */
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
        /* ------------------------------
           외부 경로 링크만 제공된 fallback
        ------------------------------ */
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

  /* ------------------------------
     거리 / 시간 문자열 처리
  ------------------------------ */

  let distanceStr = "";
  let timeStr = "";

  if (pathData && "pathSummary" in pathData.data) {
    const { totalDistance, totalTime } = pathData.data.pathSummary;

    distanceStr = totalDistance >= 1000 ? `${(totalDistance / 1000).toFixed(1)} km` : `${totalDistance} m`;

    const hours = Math.floor(totalTime / 3600);
    const minutes = Math.floor((totalTime % 3600) / 60);

    timeStr = hours > 0 ? `${hours}시간 ${minutes}분` : minutes > 0 ? `${minutes}분` : `${totalTime}초`;
  }

  /* ------------------------------
     화면 렌더링
  ------------------------------ */

  return (
      <div style={{ position: "relative", width: "100%", height: "100vh" }}>
        <div id="map" style={{ width: "100%", height: "100%" }} />

        {/* 내부 경로 */}
        {pathData && "pathSummary" in pathData.data && (
            <div
                style={{
                  position: "absolute",
                  bottom: "10px",
                  left: "0",
                  width: "100%",
                  padding: "12px",
                  backgroundColor: "#FFFFFF",
                  zIndex: 10,
                  boxSizing: "border-box",
                  borderTop: "1px solid #ccc",
                }}
            >
              <p>
                총 거리: <strong>{distanceStr}</strong>
                <br />
                예상 소요 시간: <strong>{timeStr}</strong>
              </p>
            </div>
        )}

        {/* Fallback 경로 */}
        {pathData && "uri" in pathData.data && (
            <div
                style={{
                  position: "absolute",
                  bottom: "10px",
                  left: "0",
                  width: "100%",
                  padding: "12px",
                  backgroundColor: "#FEEDBE",
                  zIndex: 10,
                  boxSizing: "border-box",
                  borderTop: "1px solid #ccc",
                }}
            >
              <p style={{ marginBottom: "8px" }}>
                ⚠️ 해당 구간은 서비스 제공 구역이 아니거나 너무 멉니다.
                <br />
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
