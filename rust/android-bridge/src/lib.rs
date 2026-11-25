use jni::objects::{JClass, JObject, JObjectArray, JString};
use jni::sys::{jobjectArray, jstring};
use jni::JNIEnv;
use std::panic::{catch_unwind, AssertUnwindSafe};
use std::ptr;

#[no_mangle]
pub extern "system" fn Java_com_sarotech_launcher_NativeBridge_nativeVersion(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    catch_unwind(AssertUnwindSafe(|| {
        env.new_string("SAROTECH Rust Core 0.1.2")
            .map(|value| value.into_raw())
            .unwrap_or(ptr::null_mut())
    })).unwrap_or(ptr::null_mut())
}

#[no_mangle]
pub extern "system" fn Java_com_sarotech_launcher_NativeBridge_nativeNormalize(
    mut env: JNIEnv,
    _class: JClass,
    input: JString,
) -> jstring {
    catch_unwind(AssertUnwindSafe(|| {
        let value: String = match env.get_string(&input) {
            Ok(value) => value.into(),
            Err(_) => return ptr::null_mut(),
        };
        env.new_string(sarotech_core::normalize_ascii_case(&value))
            .map(|value| value.into_raw())
            .unwrap_or(ptr::null_mut())
    })).unwrap_or(ptr::null_mut())
}

#[no_mangle]
pub extern "system" fn Java_com_sarotech_launcher_NativeBridge_nativeSort(
    mut env: JNIEnv,
    _class: JClass,
    input: JObjectArray,
) -> jobjectArray {
    catch_unwind(AssertUnwindSafe(|| {
        let length = match env.get_array_length(&input) {
            Ok(value) => value,
            Err(_) => return ptr::null_mut(),
        };

        let mut values = Vec::<String>::with_capacity(length as usize);
        for index in 0..length {
            let object = match env.get_object_array_element(&input, index) {
                Ok(value) => value,
                Err(_) => return ptr::null_mut(),
            };
            let string = JString::from(object);
            let value: String = match env.get_string(&string) {
                Ok(value) => value.into(),
                Err(_) => return ptr::null_mut(),
            };
            values.push(value);
        }

        sarotech_core::sort_labels(&mut values);

        let string_class = match env.find_class("java/lang/String") {
            Ok(value) => value,
            Err(_) => return ptr::null_mut(),
        };
        let output = match env.new_object_array(length, string_class, JObject::null()) {
            Ok(value) => value,
            Err(_) => return ptr::null_mut(),
        };

        for (index, value) in values.iter().enumerate() {
            let java_string = match env.new_string(value) {
                Ok(value) => value,
                Err(_) => return ptr::null_mut(),
            };
            if env.set_object_array_element(&output, index as i32, java_string).is_err() {
                return ptr::null_mut();
            }
        }
        output.into_raw()
    })).unwrap_or(ptr::null_mut())
}
