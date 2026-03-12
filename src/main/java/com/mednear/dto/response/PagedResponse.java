package com.mednear.dto.response;

import org.springframework.data.domain.Page;
import java.util.List;

/**
 * Wraps a Spring Data Page into a clean JSON object.
 * Used for all paginated endpoints (search, inventory list, etc.).
 */
public class PagedResponse<T> {

    private List<T> content;
    private int     page;
    private int     size;
    private long    totalElements;
    private int     totalPages;
    private boolean last;

    public static <T> PagedResponse<T> from(Page<T> page) {
        PagedResponse<T> r = new PagedResponse<>();
        r.content       = page.getContent();
        r.page          = page.getNumber();
        r.size          = page.getSize();
        r.totalElements = page.getTotalElements();
        r.totalPages    = page.getTotalPages();
        r.last          = page.isLast();
        return r;
    }

    public List<T> getContent()       { return content; }
    public int     getPage()          { return page; }
    public int     getSize()          { return size; }
    public long    getTotalElements() { return totalElements; }
    public int     getTotalPages()    { return totalPages; }
    public boolean isLast()           { return last; }
}
