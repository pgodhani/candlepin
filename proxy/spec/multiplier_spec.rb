require 'candlepin_scenarios'

describe 'Multiplier Products' do
  include CandlepinMethods
  it_should_behave_like 'Candlepin Scenarios'

  before(:each) do
    @owner = create_owner 'some_owner'
    @user = user_client(@owner, 'guy')
  end

  it 'should have the correct quantity' do
    calendaring = create_product('8723775392', 'Calendaring - 25 Pack', :multiplier => 25)
    @cp.create_subscription(@owner.key, calendaring.id, 4)
    @cp.refresh_pools @owner.key

    pools = @user.list_pools

    pools.should have(1).things
    pools.first.quantity.should == 100
  end

  it 'should default the multiplier to 1 if it is negative' do
    product = create_product('23049', 'Some Product', :multiplier => -10)
    @cp.create_subscription(@owner.key, product.id, 34)
    @cp.refresh_pools @owner.key

    pools = @user.list_pools

    pools.should have(1).things
    pools.first.quantity.should == 34
  end

  it 'should default the multiplier to 1 if it is zero' do
    product = create_product('9382533329', 'Some Other Product', :multiplier => 0)
    @cp.create_subscription(@owner.key, product.id, 18)
    @cp.refresh_pools @owner.key

    pools = @user.list_pools

    pools.should have(1).things
    pools.first.quantity.should == 18
  end
end