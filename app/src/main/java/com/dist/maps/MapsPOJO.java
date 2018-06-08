package com.dist.maps;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MapsPOJO {

    @SerializedName("routes")
    @Expose
    private List<Route> routes = null;

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }



    public class Route {

        @SerializedName("legs")
        @Expose
        private List<Leg> legs = null;

        public List<Leg> getLegs() {
            return legs;
        }

        public void setLegs(List<Leg> legs) {
            this.legs = legs;
        }


        public class Leg {

            @SerializedName("distance")
            @Expose
            private Distance distance;
            @SerializedName("duration")
            @Expose
            private Duration duration;

            public Distance getDistance() {
                return distance;
            }

            public void setDistance(Distance distance) {
                this.distance = distance;
            }

            public Duration getDuration() {
                return duration;
            }

            public void setDuration(Duration duration) {
                this.duration = duration;
            }

        }
    }
}
