"use client";

import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';

interface PathNode {
  lat: number;
  lng: number;
}
interface PathSummary {
  distance: number;   // total distance in meters
  duration: number;   // total duration in seconds
}
interface PathResponse {
  pathNodeList: PathNode[];
  pathSummary: PathSummary;
}

const DirectionsPage: React.FC = () => {
  const searchParams = useSearchParams();
  const [pathData, setPathData] = useState<PathResponse | null>(null);

  useEffect(() => {
    const startLatStr = searchParams.get('start-lat');
    const startLngStr = searchParams.get('start-lng');
    const endLatStr = searchParams.get('end-lat');
    const endLngStr = searchParams.get('end-lng');
    const startName = searchParams.get('start-name');
    const endName = searchParams.get('end-name'); // TODO: mock 데이터에서 추출한 이름, 이후 실제 데이터 반영 필요

    if (!startLatStr || !startLngStr || !endLatStr || !endLngStr || !startName || !endName) {
      return;
    }

    const startLat = Number(startLatStr);
    const startLng = Number(startLngStr);
    const endLat = Number(endLatStr);
    const endLng = Number(endLngStr);

    const requestBody = {
      startY: startLat,
      startX: startLng,
      endY: endLat,
      endX: endLng,
      startName,
      endName
    };

    const fetchPath = async () => {
      try {
        const res = await fetch("/api/v1/paths", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(requestBody)
        });
        if (!res.ok) {
          console.error("Failed to fetch path data");
          return;
        }
        const data: PathResponse = await res.json();
        setPathData(data);
      } catch (error) {
        console.error("Error fetching path data:", error);
      }
    };

    fetchPath();
  }, [searchParams]);

  useEffect(() => {
    if (!pathData) return;
    const { pathNodeList } = pathData;

    const drawMap = () => {
      if (!pathNodeList || pathNodeList.length === 0) return;
      const mapContainer = document.getElementById("map");
      if (!mapContainer) return;

      const kakaoMaps = (window as any).kakao.maps;
      const startCoord = new kakaoMaps.LatLng(pathNodeList[0].lat, pathNodeList[0].lng);
      const mapOptions = { center: startCoord, level: 5 };
      const map = new kakaoMaps.Map(mapContainer, mapOptions);

      new kakaoMaps.Marker({ position: startCoord, map });
      const endCoord = new kakaoMaps.LatLng(pathNodeList[pathNodeList.length - 1].lat, pathNodeList[pathNodeList.length - 1].lng);
      new kakaoMaps.Marker({ position: endCoord, map });

      const linePath = pathNodeList.map(node => new kakaoMaps.LatLng(node.lat, node.lng));
      new kakaoMaps.Polyline({
        path: linePath,
        strokeWeight: 5,
        strokeColor: "#005AFF",
        strokeOpacity: 0.8,
        strokeStyle: "solid",
        map
      });

      const bounds = new kakaoMaps.LatLngBounds();
      linePath.forEach(point => bounds.extend(point));
      map.setBounds(bounds);
    };

    if (!(window as any).kakao || !(window as any).kakao.maps) {
      const script = document.createElement("script");
      const kakaoApiKey = process.env.NEXT_PUBLIC_KAKAO_MAP_KEY;
      if (!kakaoApiKey) {
        console.error("Kakao Maps API key is missing.");
        return;
      }
      script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoApiKey}&autoload=false`;
      script.onload = () => {
        (window as any).kakao.maps.load(drawMap);
      };
      document.head.appendChild(script);
    } else {
      (window as any).kakao.maps.load(drawMap);
    }
  }, [pathData]);

  let distanceStr = "";
  let timeStr = "";
  if (pathData?.pathSummary) {
    const { distance, duration } = pathData.pathSummary;
    distanceStr = distance >= 1000 ? `${(distance / 1000).toFixed(1)} km` : `${distance} m`;
    const hours = Math.floor(duration / 3600);
    const minutes = Math.floor((duration % 3600) / 60);
    timeStr = hours > 0 ? `${hours}시간 ${minutes}분` : minutes > 0 ? `${minutes}분` : `${duration}초`;
  }

  return (
      <div>
        <div id="map" style={{ width: "100%", height: "80vh" }} />
        {distanceStr && timeStr && (
            <div style={{ padding: "10px", fontSize: "16px" }}>
              <p>총 거리: <strong>{distanceStr}</strong>, 예상 소요 시간: <strong>{timeStr}</strong></p>
            </div>
        )}
      </div>
  );
};

export default DirectionsPage;
