package com.nowcoder.community.util;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger= LoggerFactory.getLogger(SensitiveFilter.class);
    private static final String REPLACEMENT="***";//替换符
    private TrieNode rootNode=new TrieNode();//根节点
    @PostConstruct
    public void init(){
        try(
                InputStream is=this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader=new BufferedReader(new InputStreamReader(is));)
        {
            String keyword;
            while((keyword= reader.readLine())!=null){
                //添加到前缀树
                this.addKeyword(keyword);
            }
        }catch(IOException e){
            logger.error("加载敏感词文件失败："+e.getMessage());
        }
    }
    //将一个敏感词添加到前缀树中
    private void addKeyword(String keyword){
        TrieNode tempNode=rootNode;
        for(int i=0;i<keyword.length();++i){
            char c=keyword.charAt(i);
            TrieNode subNode=tempNode.getSubNode(c);

            if(subNode==null){
                subNode=new TrieNode();
                tempNode.addSubNode(c,subNode);
            }

            //指针指向子节点进入下一轮循环
            tempNode=subNode;
            //设置结束标识
            if(i==keyword.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }

        //指针1指向树
        TrieNode tempNode=rootNode;
        //指针2指向敏感词字符串头
        int begin=0;
        //指针3指向敏感词字符串尾部
        int position=0;
        //记录结果
        StringBuilder sb=new StringBuilder();

        while(position<text.length()){
            if(position<text.length()){
                char c=text.charAt(position);
                //跳过符号
                if(isSymbol(c)){
                    //指针1处于根节点，指针2指针3一起移动；否则只移动指针3
                    if(tempNode==rootNode){
                        sb.append(c);
                        begin++;
                    }
                    position++;
                    continue;
                }
                //检查下级节点
                tempNode=tempNode.getSubNode(c);
                if(tempNode==null){//以begin开头的字符串不是敏感词
                    sb.append(text.charAt(begin));
                    position=++begin;
                    tempNode=rootNode;
                }else if(tempNode.isKeywordEnd()){//发现敏感词
                    sb.append(REPLACEMENT);
                    begin=++position;
                    tempNode=rootNode;
                }else{//检查下一个字符
                    position++;
                }
            }else{
                sb.append(text.charAt(begin));
                position=++begin;
                tempNode=rootNode;
            }
        }
        //将最后一批字符计入结果
        //sb.append(text.substring(begin));
        return sb.toString();
    }
    private boolean isSymbol(Character c){
        //0x2E80~0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c<0x2E80 || c>0x9FFF);
    }
    private class TrieNode{
        //关键词结束标识
        private boolean isKeywordEnd=false;

        //子节点(key是下级字符，value是下级节点）
        private Map<Character,TrieNode> subNodes=new HashMap<>();
        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }

        //获取子节点
        public  TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }
}
