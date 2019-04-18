/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.samples.remoting;

import io.jmix.core.DataManager;
import io.jmix.samples.remoting.entity.Foo;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("foo")
@Profile("!remoting || client")
public class FooController {

    @Inject
    private DataManager dataManager;

    @PostMapping(path = "/create", produces = "application/json")
    Foo createFoo() {
        Foo foo = dataManager.create(Foo.class);
        foo.setName("Foo-" + LocalDateTime.now().toString());
        return dataManager.commit(foo);
    }

    @GetMapping(path = "/all", produces = "application/json")
    List<Foo> getAll() {
        return dataManager.load(Foo.class).list();
    }
}
