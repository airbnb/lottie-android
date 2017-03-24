package com.airbnb.lottie;

import android.annotation.TargetApi;
import android.graphics.Path;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.KITKAT)
class MergePathsContent implements PathContent {
  private final Path firstPath = new Path();
  private final Path remainderPath = new Path();
  private final Path path = new Path();

  private final String name;
  private final List<PathContent> pathContents = new ArrayList<>();
  private final MergePaths mergePaths;

  MergePathsContent(MergePaths mergePaths) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
      throw new IllegalStateException("Merge paths are not supported pre-KitKat.");
    }
    name = mergePaths.getName();
    this.mergePaths = mergePaths;
  }

  void addContentIfNeeded(Content content) {
    if (content instanceof PathContent) {
      pathContents.add((PathContent) content);
    }
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    for (int i = 0; i < pathContents.size(); i++) {
      pathContents.get(i).setContents(contentsBefore, contentsAfter);
    }
  }

  @Override public Path getPath() {
    path.reset();

    switch (mergePaths.getMode()) {
      case Merge:
        addPaths();
        break;
      case Add:
        opFirstPathWithRest(Path.Op.UNION);
        break;
      case Subtract:
        opFirstPathWithRest(Path.Op.REVERSE_DIFFERENCE);
        break;
      case Intersect:
        opFirstPathWithRest(Path.Op.INTERSECT);
        break;
      case ExcludeIntersections:
        opFirstPathWithRest(Path.Op.XOR);
        break;
    }

    return path;
  }

  @Override public String getName() {
    return name;
  }

  private void addPaths() {
    for (int i = 0; i < pathContents.size(); i++) {
      path.addPath(pathContents.get(i).getPath());
    }
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  private void opFirstPathWithRest(Path.Op op) {
    remainderPath.reset();
    firstPath.reset();

    for (int i = pathContents.size() - 1; i >= 1; i--) {
      PathContent content = pathContents.get(i);

      if (content instanceof ContentGroup) {
        List<PathContent> pathList = ((ContentGroup) content).getPathList();
        for (int j = pathList.size() - 1; j >= 0; j--) {
          Path path = pathList.get(j).getPath();
          path.transform(((ContentGroup) content).getTransformationMatrix());
          this.remainderPath.addPath(path);
        }
      } else {
        remainderPath.addPath(content.getPath());
      }
    }

    PathContent lastContent = pathContents.get(0);
    if (lastContent instanceof ContentGroup) {
      List<PathContent> pathList = ((ContentGroup) lastContent).getPathList();
      for (int j = 0; j < pathList.size(); j++) {
        Path path = pathList.get(j).getPath();
        path.transform(((ContentGroup) lastContent).getTransformationMatrix());
        this.firstPath.addPath(path);
      }
    } else {
      firstPath.set(lastContent.getPath());
    }

    path.op(firstPath, remainderPath, op);
  }
}
