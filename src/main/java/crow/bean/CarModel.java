package crow.bean;

/**
 * @ProjectName: crawltruckhome
 * @ClassName: CarModel
 * @Description: 车型
 * @Author: ZhangJun
 * @Date: 2019/10/23 14:28
 */
public class CarModel {
    private String brandId;
    private String brandSeriesId;
    private String id;
    private String name;
    private String status;//在售，未上市，停售
    private String link;

    public String getLink() {
        return link;
    }

    public CarModel setLink(String link) {
        this.link = link;
        return this;
    }

    public String getBrandId() {
        return brandId;
    }

    public CarModel setBrandId(String brandId) {
        this.brandId = brandId;
        return this;
    }

    public String getBrandSeriesId() {
        return brandSeriesId;
    }

    public CarModel setBrandSeriesId(String brandSeriesId) {
        this.brandSeriesId = brandSeriesId;
        return this;
    }

    public String getId() {
        return id;
    }

    public CarModel setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public CarModel setName(String name) {
        this.name = name;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public CarModel setStatus(String status) {
        this.status = status;
        return this;
    }
}
