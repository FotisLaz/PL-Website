package com.example.dataprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerData {

    @JsonProperty("name")
    private String name;

    @JsonProperty("nation")
    private String nation;

    @JsonProperty("pos")
    private String pos;

    @JsonProperty("age")
    private Integer age;

    @JsonProperty("mp")
    private Integer mp;

    @JsonProperty("starts")
    private Integer starts;

    @JsonProperty("min")
    private Double min;

    @JsonProperty("gls")
    private Double gls;

    @JsonProperty("ast")
    private Double ast;

    @JsonProperty("pk")
    private Double pk;

    @JsonProperty("crdy")
    private Double crdy;

    @JsonProperty("crdr")
    private Double crdr;

    @JsonProperty("xg")
    private Double xg;

    @JsonProperty("xa")
    private Double xa;

    @JsonProperty("team")
    private String team;

    // Constructors
    public PlayerData() {
    }

    public PlayerData(String name, String nation, String pos, Integer age,
            Integer mp, Integer starts, Double min, Double gls,
            Double ast, Double pk, Double crdy, Double crdr,
            Double xg, Double xa, String team) {
        this.name = name;
        this.nation = nation;
        this.pos = pos;
        this.age = age;
        this.mp = mp;
        this.starts = starts;
        this.min = min;
        this.gls = gls;
        this.ast = ast;
        this.pk = pk;
        this.crdy = crdy;
        this.crdr = crdr;
        this.xg = xg;
        this.xa = xa;
        this.team = team;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getMp() {
        return mp;
    }

    public void setMp(Integer mp) {
        this.mp = mp;
    }

    public Integer getStarts() {
        return starts;
    }

    public void setStarts(Integer starts) {
        this.starts = starts;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getGls() {
        return gls;
    }

    public void setGls(Double gls) {
        this.gls = gls;
    }

    public Double getAst() {
        return ast;
    }

    public void setAst(Double ast) {
        this.ast = ast;
    }

    public Double getPk() {
        return pk;
    }

    public void setPk(Double pk) {
        this.pk = pk;
    }

    public Double getCrdy() {
        return crdy;
    }

    public void setCrdy(Double crdy) {
        this.crdy = crdy;
    }

    public Double getCrdr() {
        return crdr;
    }

    public void setCrdr(Double crdr) {
        this.crdr = crdr;
    }

    public Double getXg() {
        return xg;
    }

    public void setXg(Double xg) {
        this.xg = xg;
    }

    public Double getXa() {
        return xa;
    }

    public void setXa(Double xa) {
        this.xa = xa;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "name='" + name + '\'' +
                ", nation='" + nation + '\'' +
                ", pos='" + pos + '\'' +
                ", age=" + age +
                ", team='" + team + '\'' +
                '}';
    }
}