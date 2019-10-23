package crow.bean;

/**
 * @ProjectName: crawltruckhome
 * @ClassName: Brand
 * @Description: 品牌
 * @Author: ZhangJun
 * @Date: 2019/10/23 14:28
 */
public class Brand {
    private String id;
    private String name;
    private String picLink;
    private String firstWord;
    private String link;

    public String getLink() {
        return link;
    }

    public Brand setLink(String link) {
        this.link = link;
        return this;
    }

    public String getId() {
        return id;
    }

    public Brand setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Brand setName(String name) {
        this.name = name;
        return this;
    }

    public String getPicLink() {
        return picLink;
    }

    public Brand setPicLink(String picLink) {
        this.picLink = picLink;
        return this;
    }

    public String getFirstWord() {
        return firstWord;
    }

    public Brand setFirstWord(String firstWord) {
        this.firstWord = firstWord;
        return this;
    }
}
