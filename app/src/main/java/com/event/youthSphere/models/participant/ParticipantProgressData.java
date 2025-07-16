package com.event.youthSphere.models.participant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParticipantProgressData {
    private int totalActivities;
    private float averageScore;
    private Map<String, Integer> domainParticipation = new HashMap<>();
    private Map<String, List<Integer>> domainAvgScores = new HashMap<>();

    public int getTotalActivities() { return totalActivities; }
    public void setTotalActivities(int totalActivities) { this.totalActivities = totalActivities; }

    public float getAverageScore() { return averageScore; }
    public void setAverageScore(float averageScore) { this.averageScore = averageScore; }

    public Map<String, Integer> getDomainParticipation() { return domainParticipation; }
    public void setDomainParticipation(Map<String, Integer> domainParticipation) { this.domainParticipation = domainParticipation; }

    public Map<String, Float> getDomainWiseAverage() {
        Map<String, Float> result = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : domainAvgScores.entrySet()) {
            List<Integer> scores = entry.getValue();
            float avg = 0f;
            if (!scores.isEmpty()) {
                int sum = 0;
                for (int r : scores) sum += r;
                avg = sum / (float) scores.size();
            }
            result.put(entry.getKey(), avg);
        }
        return result;
    }

    public void setDomainAvgScores(Map<String, List<Integer>> domainAvgScores) {
        this.domainAvgScores = domainAvgScores;
    }
}

