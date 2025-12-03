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
    // Extract required query parameters
    const startLatStr = searchParams.get('start-lat');
    const startLngStr = searchParams.get('start-lng');
    const endLatStr = searchParams.get('end-lat');
    const endLngStr = searchParams.get('end-lng');
    const startName = searchParams.get('start-name');  // expected to be "현재 위치"
    const endName = searchParams.get('end-name');      // TODO: using station name from mock data; may change in future

    // All fields are required – if any are missing, do nothing (no error handling as per requirements)
    if (!startLatStr || !startLngStr || !endLatStr || !endLngStr || !startName || !endName) {
      return;
    }

    // Convert latitude/longitude to numbers
    const startLat = Number(startLatStr);
    const startLng = Number(startLngStr);
    const endLat = Number(endLatStr);
    const endLng = Number(endLngStr);

    // Prepare request payload
    const requestBody = {
      startLat,
      startLng,
      endLat,
      endLng,
      startName,
      endName
    };

    // Send POST request to the backend Directions API
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
    const { pathNodeList, pathSummary } = pathData;

    // Function to initialize Kakao Map and draw the route
    const drawMap = () => {
      if (!pathNodeList || pathNodeList.length === 0) return;
      const mapContainer = document.getElementById("map");
      if (!mapContainer) return;

      // Initialize map centered at the start coordinate
      const kakaoMaps = (window as any).kakao.maps;
      const startCoord = new kakaoMaps.LatLng(pathNodeList[0].lat, pathNodeList[0].lng);
      const mapOptions = { center: startCoord, level: 5 };  // level 5 for a reasonably close zoom
      const map = new kakaoMaps.Map(mapContainer, mapOptions);

      // Place start marker (using first node as start)
      new kakaoMaps.Marker({
        position: startCoord,
        map: map
      });
      // Place end marker (using last node as end)
      const endCoord = new kakaoMaps.LatLng(pathNodeList[pathNodeList.length - 1].lat, pathNodeList[pathNodeList.length - 1].lng);
      new kakaoMaps.Marker({
        position: endCoord,
        map: map
      });

      // Draw route polyline connecting all path nodes
      const linePath = pathNodeList.map(node => new kakaoMaps.LatLng(node.lat, node.lng));
      new kakaoMaps.Polyline({
        path: linePath,
        strokeWeight: 5,       // line thickness
        strokeColor: "#005AFF",// line color (blue)
        strokeOpacity: 0.8,    // line transparency
        strokeStyle: "solid",  // line style
        map: map
      });

      // Adjust map viewport to show the entire polyline
      const bounds = new kakaoMaps.LatLngBounds();
      linePath.forEach(point => bounds.extend(point));
      map.setBounds(bounds);
    };

    // Dynamically load Kakao Maps JavaScript SDK if not already loaded
    if (!(window as any).kakao || !(window as any).kakao.maps) {
      const script = document.createElement("script");
      const kakaoApiKey = process.env.NEXT_PUBLIC_KAKAO_MAP_KEY;  // Kakao Maps JavaScript API Key
      if (!kakaoApiKey) {
        console.error("Kakao Maps API key is missing. Set NEXT_PUBLIC_KAKAO_MAP_KEY in your environment.");
      }
      script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoApiKey || ""}&autoload=false`;
      script.onload = () => {
        // Load the maps library, then execute drawMap
        (window as any).kakao.maps.load(drawMap);
      };
      document.head.appendChild(script);
    } else {
      // If Kakao Maps script is already loaded, just draw the map
      (window as any).kakao.maps.load(drawMap);
    }
  }, [pathData]);

  // Compute readable distance and time strings (with unit conversion)
  let distanceStr = "";
  let timeStr = "";
  if (pathData?.pathSummary) {
    const { distance, duration } = pathData.pathSummary;
    // Distance: meters -> km if over 1000m
    distanceStr = distance >= 1000
        ? `${(distance / 1000).toFixed(1)} km`
        : `${distance} m`;
    // Duration: seconds -> convert to hours/minutes for display
    const hours = Math.floor(duration / 3600);
    const minutes = Math.floor((duration % 3600) / 60);
    if (hours > 0) {
      const remainingMinutes = Math.floor((duration % 3600) / 60);
      timeStr = `${hours}시간 ${remainingMinutes}분`;
    } else if (minutes > 0) {
      timeStr = `${minutes}분`;
    } else {
      timeStr = `${duration}초`;
    }
  }

  return (
      <div>
        {/* Map container (fills most of the viewport) */}
        <div id="map" style={{ width: "100%", height: "80vh" }} />
        {/* Display route distance and time summary */}
        {distanceStr && timeStr && (
            <div style={{ padding: "10px", fontSize: "16px" }}>
              <p>총 거리: <strong>{distanceStr}</strong>, 예상 소요 시간: <strong>{timeStr}</strong></p>
            </div>
        )}
      </div>
  );
};

export default DirectionsPage;
