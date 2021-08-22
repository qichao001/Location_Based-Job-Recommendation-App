package com.laioffer.job.recommendation;

import com.laioffer.job.db.MySQLConnection;
import com.laioffer.job.entity.Item;
import com.laioffer.job.external.GitHubClient;

import java.util.*;

public class Recommendation {
    public List<Item> recommendItems(String userId, double lat, double lon) {
        List<Item> recommendations = new ArrayList<>();

        // step1 get all favorite itemIds
        MySQLConnection conn = new MySQLConnection();
        Set<String> favoriteItemIds = conn.getFavoriteItemIds(userId);

        // step2 get all keywords, sorted by count
        Map<String, Integer> allKeywords = new HashMap<>();
        for (String itemId : favoriteItemIds) {
            Set<String> keywords = conn.getKeywords(itemId);
            for (String keyword : keywords) {
                allKeywords.put(keyword, allKeywords.getOrDefault(keyword, 0) + 1);
            }
        }
        conn.close();
        List<Map.Entry<String, Integer>> keywordList = new ArrayList<>(allKeywords.entrySet());
        keywordList.sort(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
                return Integer.compare(e2.getValue(), e1.getValue());
            }
        });
        // cut down search list only top 3
        if (keywordList.size() > 3) {
            keywordList = keywordList.subList(0, 3);
        }

        // step3 search based on keywords, filter out favarite items
        Set<String> visitedItemIds = new HashSet<>();
        GitHubClient client = new GitHubClient();
        for (Map.Entry<String, Integer> keyword : keywordList) {
            List<Item> items = client.search(lat, lon, keyword.getKey());
            for (Item item : items) {
                if (!favoriteItemIds.contains(item.getId()) && !visitedItemIds.contains(item.getId())) {
                    recommendations.add(item);
                    visitedItemIds.add(item.getId());
                }
            }
        }
        return recommendations;
    }
}
