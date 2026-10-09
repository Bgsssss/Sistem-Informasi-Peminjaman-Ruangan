package com.zedd.rpl.model.booking.res;
public interface UsageReportRow {
  Long getRoomId();
  String getRoom();
  String getType();
  Long getTotalBookings();
  Long getTotalAttended();
  Long getTotalNoShows();
  Long getTotalOtherCancellations();
}

