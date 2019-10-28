package com.sap.businesscard;

import android.content.Context;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.concurrent.CompletableFuture;

public class MyArNode extends AnchorNode {

    private AugmentedImage image;
    private static CompletableFuture<ModelRenderable> modelRenderableCompletableFuture;

    public MyArNode(Context ctx, int modelId) {
        if (modelRenderableCompletableFuture == null) {
            modelRenderableCompletableFuture = ModelRenderable.builder().setRegistryId("my_model").setSource(ctx, R.raw.dino).build();
        }
    }

    public void setImage(final AugmentedImage image) {
        this.image = image;
        if(!modelRenderableCompletableFuture.isDone()) {
            CompletableFuture.allOf(modelRenderableCompletableFuture).thenAccept((Void aVoid) -> setImage(image)).exceptionally(throwable -> null);
        }
        Node node = new Node();
        Pose pose = Pose.makeTranslation(0.0f, 0f, 0.25f);
        node.setParent(this);
        node.setLocalPosition(this.getLocalPosition());
        node.setLocalRotation(this.getLocalRotation());
        node.setLocalScale(this.getLocalScale());
        node.setRenderable(modelRenderableCompletableFuture.getNow(null));
    }


}


