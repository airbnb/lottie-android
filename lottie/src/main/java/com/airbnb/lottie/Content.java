package com.airbnb.lottie;

import java.util.List;

public interface Content {
  void setContents(List<Content> contentsBefore, List<Content> contentsAfter);
}
