package com.airbnb.lottie;

import java.util.List;

interface Content {
  void setContents(List<Content> contentsBefore, List<Content> contentsAfter);
}
