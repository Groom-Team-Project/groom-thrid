package groom.backend.domain.location.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class LocationController {

    @GetMapping("/locations")
    public String getLocations() {
        return "List of locations";
    }


    @GetMapping("/kakao/key")
    public String getKey() {
        return "32f825b60a03b505712a82f7faefe59b";
    }
}
