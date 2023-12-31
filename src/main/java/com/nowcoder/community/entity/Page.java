package com.nowcoder.community.entity;

public class Page {
    private int current=1;//当前页码
    private int limit=10;//显示上限
    private int rows;//数据总数，用于计算总页数
    private String path;//查询路径，用于复用分页路径

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current>=1){
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit>=1&&limit<=100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getOffset(){//获取当前页的起始行
        return (current-1)*limit;
    }

    public int getTotal(){//获取总页数
        if(rows%limit==0){
            return rows/limit;
        }
        else{
            return rows/limit+1;
        }
    }

    public int getFrom(){//获取起始页码
        int from=current-2;
        return from<1?1:from;
    }
    public int getTo(){//获取结束页码
        int to=current+2;
        int total=getTotal();
        return to>total?total:to;
    }
}
