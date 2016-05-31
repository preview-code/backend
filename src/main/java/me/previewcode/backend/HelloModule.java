package me.previewcode.backend;

import com.google.inject.Module;
import com.google.inject.Binder;

public class HelloModule implements Module
{
    public void configure(final Binder binder)
    {
       binder.bind(MessageRestService.class);
    }
}
