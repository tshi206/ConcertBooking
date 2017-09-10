package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.NewsItemDTO;

import java.util.HashMap;
import java.util.Map;

public class LocalNewsItemStorage implements ConcertService.NewsItemListener {

    private Map<Long, NewsItemDTO> newsItemDTOMap = new HashMap<>();

    @Override
    public void newsItemReceived(NewsItemDTO newsItem) {
        newsItemDTOMap.put(newsItem.getId(), newsItem);
    }

    public Map<Long, NewsItemDTO> getNewsItemDTOMap() {
        return newsItemDTOMap;
    }

}
