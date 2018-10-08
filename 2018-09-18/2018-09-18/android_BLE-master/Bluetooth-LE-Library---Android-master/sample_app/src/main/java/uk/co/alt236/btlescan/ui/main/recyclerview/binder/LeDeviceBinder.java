package uk.co.alt236.btlescan.ui.main.recyclerview.binder;

import android.content.Context;
import android.view.View;

import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.btlescan.ui.common.Navigation;
import uk.co.alt236.btlescan.ui.common.recyclerview.BaseViewBinder;
import uk.co.alt236.btlescan.ui.common.recyclerview.BaseViewHolder;
import uk.co.alt236.btlescan.ui.common.recyclerview.RecyclerViewItem;
import uk.co.alt236.btlescan.ui.main.recyclerview.holder.LeDeviceHolder;
import uk.co.alt236.btlescan.ui.main.recyclerview.model.LeDeviceItem;

public class LeDeviceBinder extends BaseViewBinder<LeDeviceItem> {

    private final Navigation navigation;

    public LeDeviceBinder(Context context, Navigation navigation) {
        super(context);
        this.navigation = navigation;
    }

    @Override
    public void bind(BaseViewHolder<LeDeviceItem> holder, LeDeviceItem item) {

        final LeDeviceHolder actualHolder = (LeDeviceHolder) holder;
        final BluetoothLeDevice device = item.getDevice();

        CommonBinding.bind(getContext(), actualHolder, device);
        //如果device设备为TESS5600，建立连接;
        if(device.get_falg_TESS5600())
        {
           // navigation.openDetailsActivity(device); 可以打开DetailActivity，查看device详细信息;
            navigation.startControlActivity(device);
        }
        //navigation是一个中间类，连接binder和BLEDevice;
    //如若要实现list click事件，取消下列注释即可;
//        actualHolder.getView().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                navigation.openDetailsActivity(device);
//            }
//        });
    }

    @Override
    public boolean canBind(RecyclerViewItem item) {
        return item instanceof LeDeviceItem;
    }
}
