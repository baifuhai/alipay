package com.test.alipay.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "trade")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "trade_no")
    private String tradeNo;

    @Column(name = "out_trade_no")
    private String outTradeNo;

    @Column(name = "total_amount")
    private String totalAmount;

    @Column(name = "subject")
    private String subject;

    @Column(name = "body")
    private String body;

    @Column(name = "refund_amount")
    private String refundAmount;

    @Column(name = "refund_reason")
    private String refundReason;

    @Column(name = "refund_no")
    private String refundNo;

    @Column(name = "status")
    private String status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }

    public String getRefundNo() {
        return refundNo;
    }

    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "id=" + id +
                ", tradeNo='" + tradeNo + '\'' +
                ", outTradeNo='" + outTradeNo + '\'' +
                ", totalAmount='" + totalAmount + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                ", refundAmount='" + refundAmount + '\'' +
                ", refundReason='" + refundReason + '\'' +
                ", refundNo='" + refundNo + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

}
