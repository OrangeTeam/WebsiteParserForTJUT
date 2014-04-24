package org.orange.parser.parser;


import org.orange.parser.entity.Post;

import java.util.Date;
import java.util.List;

public abstract class BasePostParser extends AbstractParser<List<Post>> {
    /** 过滤器：最大通知数目 */
    protected Long mFilterMax;
    /** 过滤器：通知最早时间，即只解析此后（包括filterMinDate）的通知 */
    protected Date mFilterMinDate;
    /** 过滤器：通知类别 */
    protected List<String> mFilterCategories;

    /**
     * 设置过滤器：最大通知数目，即最多只解析这么多的通知
     * @param filterMax 最大通知数目；可以设置为null以取消之前设置的过滤器
     * @return this
     */
    public BasePostParser setFilterMax(Long filterMax) {
        if (filterMax != null && filterMax < 0) {
            throw new IllegalArgumentException("filterMax < 0");
        }
        mFilterMax = filterMax;
        return this;
    }

    /**
     * 设置过滤器：通知最早时间，即只解析此后（包括filterMinDate）的通知
     * @param filterMinDate 通知最早时间；可以设置为null以取消之前设置的过滤器
     * @return this
     */
    public BasePostParser setFilterMinDate(Date filterMinDate) {
        mFilterMinDate = filterMinDate;
        return this;
    }

    /**
     * 添加过滤器：通知类别
     * @param filterCategories 要解析的通知类别列表。本对象保留其引用，即对其的修改影响本对象；
     *                         可以设置为null以取消之前设置的过滤器
     */
    public BasePostParser setFilterCategories(List<String> filterCategories) {
        mFilterCategories = filterCategories;
        return this;
    }

}
