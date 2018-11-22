/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.hiteshlilhare.jcpss.util;

import com.github.hiteshlilhare.jcpss.bean.GitHubRelease;
import java.time.LocalDateTime;
import java.util.Comparator;

/**
 *
 * @author Hitesh
 */
public class SortByLocalDateTime implements Comparator<GitHubRelease> {

    @Override
    public int compare(GitHubRelease obj1, GitHubRelease obj2) {
        LocalDateTime obj1ReleaseDateTime = Util.getLocalDateTime(obj1.getPublishedAt());
        LocalDateTime obj2ReleaseDateTime = Util.getLocalDateTime(obj2.getPublishedAt());
        if (obj1ReleaseDateTime.isAfter(obj2ReleaseDateTime)) {
            return 1;
        } else if (obj1ReleaseDateTime.isBefore(obj2ReleaseDateTime)) {
            return -1;
        } else {
            return 0;
        }
    }

}
