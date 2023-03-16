package com.hoctap.moviesstore;

public class Movies {
    private String imgSource;
    private String title;
    private String price;

    /**
     * Hàm tạo đối tượng Movies
     * @param imgSource
     * @param title
     * @param price
     */
    public Movies(String imgSource, String title, String price) {
        this.imgSource = imgSource;
        this.title = title;
        this.price = price;
    }

    /**
     * Lấy thông tin là địa chỉ của hình ảnh dưới dạng string( Đường dẫn tới hình ảnh).
     * @return
     */
    public String getImgSource() {
        return imgSource;
    }

    /**
     * Thiết lập địa chỉ cảu hình ảnh
     * @param imgSource
     */
    public void setImgSource(String imgSource) {
        this.imgSource = imgSource;
    }

    /**
     * Lấy thông tin là tiêu để của Movie
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * Thiết lập tiêu đề của một movie
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Lấy giá của một movie
     * @return
     */
    public String getPrice() {
        return price;
    }

    /**
     * Thiết lập giá của một movie
     * @param price
     */
    public void setPrice(String price) {
        this.price = price;
    }
}
