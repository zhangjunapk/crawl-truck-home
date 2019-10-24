package crow;

import com.alibaba.fastjson.JSONObject;
import crow.bean.Brand;
import crow.bean.BrandSeries;
import crow.bean.CarModel;
import crow.bean.CarSubtype;
import crow.util.DBUtil;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.sound.midi.Soundbank;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.Buffer;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @BelongsProject: crowdycw
 * @BelongsPackage: org.zj.crow
 * @Author: ZhangJun
 * @CreateTime: 2019/1/5
 * @Description: ${Description}
 */
public class Content {


    static PhantomJSDriver driver;
    private static final String baseUrl="https://www.iautos.cn";
    private static final String link = "https://www.iautos.cn/chexing/";

    static Map<String, String> headMap = new HashMap<>();

    private static List<Brand> brands=new ArrayList<>();
    private static List<BrandSeries> brandSeriess=new ArrayList<>();
    private static List<CarModel> carModels=new ArrayList<>();
    private static List<CarSubtype> carSubtypes=new ArrayList<>();
    public static void main(String[] args) throws Exception {
        //generateJava();
        //接下来我要开始爬取列表页面
      //  crawListPage();
        //接下来放进去看看
    //    writeToFile();

        //接下来就是处理子类型
        //crawlSubType();
        //接下来写入到文件
      //  writeToFile();
      //  createTable();
        //生成插入数据的sql并执行
        runSql();
    }

    private static void runSql() {
        DBUtil dbUtil = new DBUtil();
        try {
            /**
             * "brand");
             * ries.java"),"brand_series");
             * ava"),"car_model");
             * pe.java"),"car_subtype");
             */
            BufferedReader br = new BufferedReader(new FileReader("D:\\temp\\crawl\\brand.json"));
            String line = "";
            while ((line = br.readLine()) != null) {
                Brand brand = JSONObject.parseObject(line, Brand.class);
                String s = genInsertSql("brand", brand);
                dbUtil.runSql(s);
            }

           /*  br = new BufferedReader(new FileReader("D:\\temp\\crawl\\brandSeries.json"));
            while ((line = br.readLine()) != null) {
                BrandSeries brandSeries = JSONObject.parseObject(line, BrandSeries.class);
                //请求这个url，获得里面所有子类型的数据
                String s = genInsertSql("brand_series", brandSeries);
                dbUtil.runSql(s);
            }*/

             br = new BufferedReader(new FileReader("D:\\temp\\crawl\\carModel.json"));
            while ((line = br.readLine()) != null) {
                CarModel carModel = JSONObject.parseObject(line, CarModel.class);
                String s = genInsertSql("car_model", carModel);
                dbUtil.runSql(s);
            }
/*
             br = new BufferedReader(new FileReader("D:\\temp\\crawl\\carSubtype.json"));
            while ((line = br.readLine()) != null) {
                CarSubtype carSubtype = JSONObject.parseObject(line, CarSubtype.class);
                String s = genInsertSql("car_subtype", carSubtype);
                dbUtil.runSql(s);
            }*/

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void crawlSubType() throws IOException {
        BufferedReader br=new BufferedReader(new FileReader("D:\\temp\\crawl\\carModel.json"));
        String line="";
        while((line=br.readLine())!=null){
            CarModel carModel = JSONObject.parseObject(line, CarModel.class);
            //请求这个url，获得里面所有子类型的数据
            requestAndParse(carModel);
        }
    }

    private static void requestAndParse(CarModel carModel) {
        if(Objects.isNull(carModel)){
            return;
        }
        String link = carModel.getLink();
        //这是车型的连接，请求这个链接获得所有子类型
        Element elementByJsoup = getElementByJsoup(link);
        parseAndInflate(carModel,elementByJsoup);
    }

    private static void parseAndInflate(CarModel carModel,Element document) {
        if(Objects.isNull(document)){
            writeAppend(new StringBuilder(carModel.getLink()).append("\r\n"),new File("D:\\temp\\crawl\\errorDetail.json"));
            return;
        }
        Elements items = document.select("#filter_content > div[class=tractor-price-content price-wrap]");
        for(Element item:items){
            Elements headers = item.select(" > div > a");
            if(!Objects.isNull(headers)&&!headers.isEmpty()){
                Element aEle = headers.get(0);
                String href = aEle.attr("href");
                String s = href.replaceAll("index", "param");
                String hreff="https://product.360che.com"+s;
                parseSubtypesDocument(carModel,hreff);
            }
        }
    }

    private static void parseSubtypesDocument(CarModel carModel,String hreff) {
        //我需要创建几个
        Element elementBySe = getElementBySe(hreff);
        Elements select = elementBySe.select("#fixed_top > th");
        if(Objects.isNull(select)||select.isEmpty()){
            return;
        }
        List<CarSubtype> result=new ArrayList<>();
        int i1 = select.size() - 1;
        for(int i=0;i<i1;i++){
            CarSubtype carSubtype = new CarSubtype();
            carSubtype.setBrandId(carModel.getBrandId());
            carSubtype.setBrandSeriesId(carModel.getBrandSeriesId());
            carSubtype.setCarModelId(carModel.getId());
            result.add(carSubtype);
        }

        //这里是名字和价格

        Elements nameEles = elementBySe.select("#fixed_top > th");
        if(!Objects.isNull(nameEles)&&!nameEles.isEmpty()){
            for(int i=1;i<nameEles.size();i++){
                Element element = nameEles.get(i);
                CarSubtype carSubtype = result.get(i-1);

                Element aEle = element.select("> div > h5 > a").get(0);
                carSubtype.setName(aEle.text());
                ///m239/59755_index.html
                String href = aEle.attr("href");
                String id=href.substring(1,href.lastIndexOf("_")).replaceAll("/","_");
                carSubtype.setId(id);
                carSubtype.setLink("https://product.360che.com"+href);
            }
        }

        //这里是价格
        Elements priceTds = elementBySe.select("#mybody > div.wrapper > div.parameter-detail > table:nth-child(1) > thead > tr:nth-child(2) > td");
        if(!Objects.isNull(priceTds)&&!priceTds.isEmpty()){
            for(int i=1;i<priceTds.size();i++){
                CarSubtype carSubtype = result.get(i-1);
                carSubtype.setPrice(priceTds.get(i).text());
            }
        }

        //下面是每个字段的数据
//#mybody > div.wrapper > div.parameter-detail > table:nth-child(1) > tbody > tr:nth-child(2)
        Elements rows = elementBySe.select("#mybody > div.wrapper > div.parameter-detail > table:nth-child(1) > tbody > tr[class=param-row]");
        //接下来就是关键步骤了
        for(Element eleRow:rows){
            //每一行
            Elements tds = eleRow.select("> td");
            if(Objects.isNull(tds)||tds.isEmpty()){
                continue;
            }
            //遍历每个td
            Element element = tds.get(0);//这个是字段名
            String fieldName = element.text();
            System.out.println("字段名:"+fieldName);
            String firstSpell = getFirstSpell(fieldName);
            System.out.println("拼写:"+firstSpell);
            for(int i=1;i<tds.size();i++){
                Element tdEle = tds.get(i);
                String value = tdEle.select(" > div").get(0).text();
                System.out.println("值:"+value);
                reflectionSet(result.get(i-1),firstSpell,value);
            }

        }
        carSubtypes.addAll(result);

        File carSubtypeFile = new File("D:\\temp\\crawl\\carSubtype.json");
        for(CarSubtype item:result){
            String s = JSONObject.toJSONString(item);
            writeAppend(new StringBuilder(s).append("\r\n"),carSubtypeFile);
        }

    }

    private static void reflectionSet(CarSubtype carSubtype, String firstSpell, String value) {
        try {
            Field declaredField = carSubtype.getClass().getDeclaredField(firstSpell);
            declaredField.setAccessible(true);
            Object o = declaredField.get(carSubtype);
            if(!Objects.isNull(o)){
                //如果已经有了，那就加1
                reflectionSet(carSubtype,firstSpell+1,value);
            }else{
                declaredField.set(carSubtype,value);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //找不到就算了
        }
    }

    private static void writeToFile() {
        File brandFile = new File("D:\\temp\\crawl\\brand.json");
        File brandSeriesFile = new File("D:\\temp\\crawl\\brandSeries.json");
        File carModelFile = new File("D:\\temp\\crawl\\carModel.json");
        File carSubtypeFile = new File("D:\\temp\\crawl\\carSubtype.json");
        for(Brand item:brands){
            String s = JSONObject.toJSONString(item);
            writeAppend(new StringBuilder(s).append("\r\n"),brandFile);
        }
      /*  for(BrandSeries item:brandSeriess){
            String s = JSONObject.toJSONString(item);
            writeAppend(new StringBuilder(s).append("\r\n"),brandSeriesFile);
        }
        for(CarModel item:carModels){
            String s = JSONObject.toJSONString(item);
            writeAppend(new StringBuilder(s).append("\r\n"),carModelFile);
        }
        for(CarSubtype item:carSubtypes){
            String s = JSONObject.toJSONString(item);
            writeAppend(new StringBuilder(s).append("\r\n"),carSubtypeFile);
        }*/
    }

    /**
     * 爬取那个列表页
     */
    private static void crawListPage(){
        Element elementBySe = getElementBySe("https://product.360che.com/BrandList.html");
        Elements bigDivItem = elementBySe.select("body > div.wrapper > div[class=xll_center2]");
        for(Element ele:bigDivItem){
            Element wordEle = ele.child(0);
            Elements aEle = wordEle.select(">a");
            String firstWord = aEle.attr("name");
            System.out.println("首字母"+firstWord);
            Elements brandEleItem = ele.select("> div[class=xll_center2_a1]");
            for(Element brandEle:brandEleItem){
                Brand brand=new Brand();
                brand.setFirstWord(firstWord);
                //这里面是每个品牌
                Elements imgEle = brandEle.select("> div[class=xll_center2_a1_z] > dl > dt > a > img");
                if(!Objects.isNull(imgEle)&&!imgEle.isEmpty()){
                    String brandLogoLink = imgEle.get(0).attr("src");
                    System.out.println(" logo链接:"+brandLogoLink);
                    brand.setPicLink(brandLogoLink);
                }else{
                    Elements imgEles = brandEle.select("> div.xll_center2_a1_z > dl > dt > a > img");
                    if(!Objects.isNull(imgEles)&&!imgEles.isEmpty()){
                        Element imgEl = imgEles.get(0);
                        String src = imgEl.attr("src");
                        System.out.println(" logo链接:"+src);
                        brand.setPicLink(src);
                    }
                }
                Elements brandEleIds = brandEle.select("> div[class=xll_center2_a1_z] > dl > dd > a");
                if(!Objects.isNull(brandEleIds)&&!brandEleIds.isEmpty()){
                    Element brandEleId = brandEleIds.get(0);
                    String href = brandEleId.attr("href");
                    System.out.println(" 品牌链接:"+href);
                    brand.setLink("https://product.360che.com"+href);
                    String s = href.replaceAll("/", "");
                    String s1 = s.replaceAll(".html", "");
                    brand.setId(s1);
                    brand.setName(brandEleId.text());
                }
                brands.add(brand);
                //接下来获得每个品牌系列
               /* BrandSeries brandSeries = new BrandSeries();
                Elements brandSeriesEles = brandEle.select("> div[class=xll_center2_a1_y] > div[class=xll_center2_a1_y1] > a");
                if(!Objects.isNull(brandSeriesEles)&&!brandSeriesEles.isEmpty()){
                    Element brandSeriesEle = brandSeriesEles.get(0);
                    if(!Objects.isNull(brandSeriesEle)){
                        String brandSeriesName = brandSeriesEle.text();
                        String href = brandSeriesEle.attr("href");
                        System.out.println("  品牌系列:"+brandSeriesName);
                        System.out.println("  品牌系列链接:"+href);

                        brandSeries.setBrandId(brand.getId());
                        brandSeries.setName(brandSeriesName);
                        brandSeries.setLink("https://product.360che.com"+href);
                        String s = href.replaceAll("/", "");
                        String s1 = s.replaceAll(".html", "");
                        brandSeries.setId(s1);
                        brandSeriess.add(brandSeries);
                    }
                }
                //接下来就是遍历所有车型了
                Elements carModelEles = brandEle.select("> div[class=xll_center2_a1_y] > div[class=xll_center2_a1_y2]");
                for(Element modelEle:carModelEles){
                    CarModel carModel = new CarModel();
                    Elements aEles = modelEle.select("> dl > dt > a");
                    if(!Objects.isNull(aEles)&&!aEles.isEmpty()){
                        Element aEleModel = aEles.get(0);
                        String carModelName = aEleModel.text();
                        String href = aEleModel.attr("href");
                        System.out.println("   车型名:"+carModelName);
                        System.out.println("   车型链接:"+href);

                        carModel.setBrandId(brand.getId());
                        carModel.setBrandSeriesId(brandSeries.getId());
                        carModel.setName(carModelName);
                        carModel.setLink("https://product.360che.com"+href);
                        String s = href.replaceAll("/", "");
                        String s1 = s.replaceAll(".html", "");
                        s1=s1.replaceAll("_index","");
                        carModel.setId(s1);
                    }
                    Elements statusEles = modelEle.select("> dl > dt > span");
                    if(Objects.isNull(statusEles)||statusEles.isEmpty()){
                        carModel.setStatus("在售");
                    }else{
                        carModel.setStatus(statusEles.get(0).text());
                    }
                    carModels.add(carModel);
                }*/
            }
        }
    }

    /**
     * 生成java bean 的方法
     */
    public static void generateJava(){
        StringBuilder sb=new StringBuilder();
        String path="E:\\dev\\project\\java\\crawltruckhome\\src\\main\\java\\crow\\bean\\CarSubtype.java";
        //这里我要生成java实体
        Element elementBySe = getElementBySe("https://product.360che.com/m280/70095_param.html");
        Elements select = elementBySe.select("#mybody > div.wrapper > div.parameter-detail.highlighted > table:nth-child(1) > tbody > tr[class=param-row]");
        for(Element ele:select){
            Elements select1 = ele.select(">td:nth-child(1)");
            String text = select1.text().replaceAll("：","");
            System.out.println(text);
            //开始写入
            sb.append("private String ").append(getFirstSpell(text)).append(";").append("//").append(text).append("\r\n");
        }
        writeAppend(sb,new File(path));
    }




    private static void createTable() throws Exception {
        String brand=genCreateTable(new File("E:\\dev\\project\\java\\crawltruckhome\\src\\main\\java\\crow\\bean\\Brand.java"),"brand");
        String brandSeries=genCreateTable(new File("E:\\dev\\project\\java\\crawltruckhome\\src\\main\\java\\crow\\bean\\BrandSeries.java"),"brand_series");
        String carModel=genCreateTable(new File("E:\\dev\\project\\java\\crawltruckhome\\src\\main\\java\\crow\\bean\\CarModel.java"),"car_model");
        String carSubtype=genCreateTable(new File("E:\\dev\\project\\java\\crawltruckhome\\src\\main\\java\\crow\\bean\\CarSubtype.java"),"car_subtype");

        DBUtil dbUtil = new DBUtil();
        dbUtil.runSql(brand);
        dbUtil.runSql(brandSeries);
        dbUtil.runSql(carModel);
        dbUtil.runSql(carSubtype);
    }

    private static String genCreateTable(File file,String tableName) throws IOException {
        BufferedReader br=new BufferedReader(new FileReader(file));
        StringBuilder sb=new StringBuilder("create table "+tableName+"(");
        String line=null;
        while((line=br.readLine())!=null){
            if(line.contains("//")){
                String[] split = line.split("//");
                String comment=split[1];
                String[] s = split[0].split(" ");
                System.out.println(Arrays.toString(s));
                String name=s[s.length-1].replace(";","");
                sb.append(" ").append(name).append(" varchar(50) comment '").append(comment).append("',");
            }
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(")");
        return sb.toString();
    }






    private static String genInsertSql(String tableName,Object obj) {
        StringBuilder beforeSb=new StringBuilder("insert into "+tableName+"(");

        for(Field f:obj.getClass().getDeclaredFields()){
            beforeSb.append(f.getName()).append(",");
        }
        beforeSb.deleteCharAt(beforeSb.length()-1);
        beforeSb.append(") values (");

        StringBuilder afterSb=new StringBuilder();
        for(Field f:obj.getClass().getDeclaredFields()){
            f.setAccessible(true);
            try {
                afterSb.append("'"+f.get(obj)+"'").append(",");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        afterSb.deleteCharAt(afterSb.length()-1);
        afterSb.append(")");
        return beforeSb.toString()+afterSb.toString();
    }

    private static Element getElementByJsoup(String url) {
        try {
            return Jsoup.connect(url).get().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void initHeadMap() {

        if (headMap.size() != 0) {
            return;
        }

        headMap.put("Content-Type", "application/json; charset=utf-8");
        headMap.put("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");
        headMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36");
    }


    private static Element getElementBySe(String url) {
        if (driver == null) {
            initDriver();
        }
        //打开页面
        driver.get(url);
        return Jsoup.parse(driver.getPageSource()).body();
    }

    private static void initDriver() {

        //设置必要参数
        DesiredCapabilities dcaps = new DesiredCapabilities();
        //ssl证书支持
        dcaps.setCapability("acceptSslCerts", true);
        //截屏支持
        dcaps.setCapability("takesScreenshot", true);
        //css搜索支持
        dcaps.setCapability("cssSelectorsEnabled", true);
        //js支持
        dcaps.setJavascriptEnabled(true);
        //驱动支持（第二参数表明的是你的phantomjs引擎所在的路径）
        dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                "E:\\dev\\plugin\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe");
        //创建无界面浏览器对象

        driver = new PhantomJSDriver(dcaps);

        //设置隐性等待（作用于全局）
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
    }

    private static Element testOne(){
        String url="https://www.iautos.cn/chexing/trim.asp?id=156034";
        return getElementBySe(url);
    }


    public static void genBean(){
        Element element=testOne();

        List<Element> jbxxs=element.select("#webpage > div:nth-child(12) > div.mainRight > div.jbxx");
        for(Element ele:jbxxs){

            List<Element> trs=ele.select("> div > table > tbody > tr");

            for(Element e:trs){

                List<Element> tds=e.select("> td");

                for(int i=0;i<tds.size();i+=2){
                    String name=tds.get(i).text();
                    System.out.println("public String "+getFirstSpell(name)+";//"+name);
                }
            }
        }

    }



    /**
     * 用于获得汉字的首拼音
     *
     * @param chinese
     * @return
     */
    public static String getFirstSpell(String chinese) {
        StringBuffer pybf = new StringBuffer();
        char[] arr = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > 128) {
                try {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat);
                    if (temp != null) {
                        pybf.append(temp[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pybf.append(arr[i]);
            }
        }
        return pybf.toString().replaceAll("\\W", "").trim();
    }

    /**
     * 将文字写入
     * @param file
     * @param str
     */
    /**
     * 写进去，添加的方式
     * @param sb
     * @param file
     */
    public static void writeAppend(StringBuilder sb,File file){
        try {
            FileWriter bw=new FileWriter(file,true);
            bw.append(sb);
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
