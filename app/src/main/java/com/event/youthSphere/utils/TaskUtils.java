package com.event.youthSphere.utils;



import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

public class TaskUtils {
    public static <TResult> Task<TResult> failure(String message) {
        return Tasks.forException(new Exception(message));
    }

    public static <TResult> Task<TResult> success(TResult result) {
        return Tasks.forResult(result);
    }

}
