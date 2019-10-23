package crow.bean;

/**
 * @ProjectName: crawltruckhome
 * @ClassName: BrandSeries
 * @Description: 品牌系列
 * @Author: ZhangJun
 * @Date: 2019/10/23 14:28
 */
public class BrandSeries {
    private String id;
    private String brandId;
    private String name;
    private String link;

    public String getLink() {
        return link;
    }

    public BrandSeries setLink(String link) {
        this.link = link;
        return this;
    }

    public String getId() {
        return id;
    }

    public BrandSeries setId(String id) {
        this.id = id;
        return this;
    }

    public String getBrandId() {
        return brandId;
    }

    public BrandSeries setBrandId(String brandId) {
        this.brandId = brandId;
        return this;
    }

    public String getName() {
        return name;
    }

    public BrandSeries setName(String name) {
        this.name = name;
        return this;
    }
}
