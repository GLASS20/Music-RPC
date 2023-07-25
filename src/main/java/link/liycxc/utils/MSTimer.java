package link.liycxc.utils;

/**
 * This file is part of Netease-CloudMusic-Getter project.
 * Copyright 2023 Liycxc
 * All Rights Reserved.
 *
 * @author Liycxc
 * 25/7/2023 下午8:29
 */
public class MSTimer {
    public long time = -1L;

    public boolean hasTimePassed(final long MS) {
        return System.currentTimeMillis() >= time + MS;
    }

    public long hasTimeLeft(final long MS) {
        return (MS + time) - System.currentTimeMillis();
    }

    public void reset() {
        time = System.currentTimeMillis();
    }
}
