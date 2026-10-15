package dev.inscribed.client;
/** Shared geometry contract, duplicated to preserve standalone compatibility. */
public record BookGeometry(int x,int y,int width,int height,int ribbonWidth,int inset) {
    public static BookGeometry calculate(int viewportWidth,int viewportHeight){
        int ribbon=64;
        int w=Math.min(520,viewportWidth-ribbon-20);
        int h=Math.min((int)(viewportHeight*.82),(int)(w/1.62));w=Math.min(w,(int)(h*1.62));
        return new BookGeometry((viewportWidth-w-ribbon)/2,(viewportHeight-h)/2,w,h,ribbon,Math.max(18,w/20));
    }
    public int ribbonHeight(int count){return Math.min(24,(height-12-(count-1)*2)/count);}
    public int ribbonY(int count){int rows=count*ribbonHeight(count)+(count-1)*2;return y+Math.min(18,height-rows-6);}
    public int leafWidth(){return width/2-2*inset;}
    public int leftX(){return x+inset;}
    public int rightX(){return x+width/2+inset;}
    public int footerY(){return y+height-inset-18;}
}
