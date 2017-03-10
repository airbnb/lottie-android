package com.airbnb.lottie;

import android.annotation.TargetApi;
import android.graphics.Path;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

class MergePathsContent implements Content, PathContent {
  private final Path path = new Path();
  private final Path path2 = new Path();

  private final List<PathContent> pathContents = new ArrayList<>();
  private final MergePaths mergePaths;

  MergePathsContent(MergePaths mergePaths) {
    this.mergePaths = mergePaths;
  }

  void addContentIfNeeded(Content content) {
    if (content instanceof PathContent) {
      pathContents.add((PathContent) content);
    }
  }

  @Override public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {

  }

  @Override public Path getPath() {
    path.reset();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      mergePaths();
    } else {
      supportMergePaths();
    }


    return path;
  }

  private void supportMergePaths() {
    for (int i = 0; i < pathContents.size(); i++) {
      path.addPath(pathContents.get(i).getPath());
    }
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  private void mergePaths() {
    switch (mergePaths.getMode()) {
      case Merge:
        opFirstPathWithRest(Path.Op.INTERSECT);
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
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  private void opFirstPathWithRest(Path.Op op) {
    if (pathContents.size() == 1) {
      path.addPath(pathContents.get(0).getPath());
      return;
    }

    for (int i = pathContents.size() - 1; i >= 1; i--) {
      PathContent content = pathContents.get(i);

      if (content instanceof ContentGroup) {
        List<PathContent> pathList = ((ContentGroup) content).getPathList();
        for (int j = pathList.size() - 1; j >= 0; j--) {
          Path path = pathList.get(j).getPath();
          path.transform(((ContentGroup) content).getTransformationMatrix());
          this.path2.op(path, Path.Op.UNION);
        }
      } else {
        path2.op(content.getPath(), Path.Op.UNION);
      }
    }

    PathContent lastContent = pathContents.get(0);
    if (lastContent instanceof ContentGroup) {
      List<PathContent> pathList = ((ContentGroup) lastContent).getPathList();
      for (int j = 0; j < pathList.size(); j++) {
        Path path = pathList.get(j).getPath();
        path.transform(((ContentGroup) lastContent).getTransformationMatrix());
        this.path.op(path, Path.Op.UNION);
      }
    } else {
      path.set(lastContent.getPath());
    }

    path.op(path2, op);
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  private void addMergePaths(Path.Op op) {
    for (int i = 0; i < pathContents.size(); i++) {
      PathContent content = pathContents.get(i);

      if (content instanceof ContentGroup) {
        List<PathContent> pathList = ((ContentGroup) content).getPathList();
        for (int j = 0; j < pathList.size(); j++) {
          Path path = pathList.get(j).getPath();
          path.transform(((ContentGroup) content).getTransformationMatrix());
          this.path.op(path, op);
        }
      } else {
        path.op(content.getPath(), op);
      }
    }
  }
}
