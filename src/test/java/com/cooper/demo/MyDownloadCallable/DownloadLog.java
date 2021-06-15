package com.cooper.demo.MyDownloadCallable;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DownloadLog{
    private Long start;
    private Long end;
    private Long curpos;
}