package ysn.com.flowlayout;

/**
 * @Author yangsanning
 * @ClassName Data
 * @Description 一句话概括作用
 * @Date 2019/3/27
 * @History 2019/3/27 author: description:
 */
public class Data {

    private String name;
    private int id;

    public Data(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
